package com.csrm.service;

import com.csrm.dto.Dtos.BookingReq;
import com.csrm.dto.Dtos.BookingView;
import com.csrm.dto.Enums.BookingStatus;
import com.csrm.dto.Enums.Role;
import com.csrm.exception.ApiException;
import com.csrm.model.Booking;
import com.csrm.model.Resource;
import com.csrm.model.User;
import com.csrm.repository.BookingRepository;
import com.csrm.repository.ResourceRepository;
import com.csrm.repository.UserRepository;
import com.csrm.security.AuthUser;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
    private final BookingRepository bookings;
    private final ResourceRepository resources;
    private final UserRepository users;
    private final AuditService audit;
    private final NotificationService notifier;

    public BookingService(BookingRepository bookings, ResourceRepository resources, UserRepository users,
                          AuditService audit, NotificationService notifier) {
        this.bookings = bookings; this.resources = resources; this.users = users;
        this.audit = audit; this.notifier = notifier;
    }

    @Transactional
    public BookingView create(AuthUser me, BookingReq req) {
        User user = users.findById(me.id()).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please log in again."));
        Resource r = resources.findById(req.resourceId() == null ? -1L : req.resourceId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Resource not found."));
        validate(r, user.role, req.startTime(), req.endTime());
        checkOverlap(me, r, req.startTime(), req.endTime(), -1L);

        Booking b = new Booking();
        b.user = user; b.resource = r; b.startTime = req.startTime(); b.endTime = req.endTime();
        b.status = BookingStatus.CONFIRMED;
        bookings.save(b);
        audit.log(me.id(), me.username(), "BOOKING_CREATED #" + b.id + " " + r.name + " " + b.startTime + " to " + b.endTime);
        notifier.notify(user, "Booking confirmed", r.name + " is booked from " + b.startTime + " to " + b.endTime + ".");
        return BookingView.from(b);
    }

    @Transactional
    public BookingView update(AuthUser me, Long id, BookingReq req) {
        Booking b = owned(me, id);
        if (b.status == BookingStatus.CANCELLED) throw new ApiException(HttpStatus.BAD_REQUEST, "A cancelled booking cannot be modified.");
        validate(b.resource, b.user.role, req.startTime(), req.endTime());
        checkOverlap(me, b.resource, req.startTime(), req.endTime(), b.id);
        b.startTime = req.startTime(); b.endTime = req.endTime(); b.reminderSent = false;
        audit.log(me.id(), me.username(), "BOOKING_MODIFIED #" + b.id + " " + b.resource.name + " " + b.startTime + " to " + b.endTime);
        notifier.notify(b.user, "Booking updated", b.resource.name + " is now " + b.startTime + " to " + b.endTime + ".");
        return BookingView.from(b);
    }

    @Transactional
    public void cancel(AuthUser me, Long id) {
        Booking b = owned(me, id);
        if (b.status == BookingStatus.CANCELLED) return;
        b.status = BookingStatus.CANCELLED;
        audit.log(me.id(), me.username(), "BOOKING_CANCELLED #" + b.id + " " + b.resource.name);
        notifier.notify(b.user, "Booking cancelled", "Your booking of " + b.resource.name + " on " + b.startTime + " was cancelled.");
    }

    @Transactional(readOnly = true)
    public List<BookingView> mine(AuthUser me) {
        return bookings.findByUser_IdOrderByStartTimeDesc(me.id()).stream().map(BookingView::from).toList();
    }

    // resourceId + date: any logged-in user (used by the booking calendar). No filters: admin only.
    @Transactional(readOnly = true)
    public List<BookingView> search(AuthUser me, Long resourceId, LocalDate date) {
        if (resourceId == null && me.role() != Role.ADMIN)
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission for this action.");
        LocalDateTime from = date == null ? LocalDateTime.of(2000, 1, 1, 0, 0) : date.atStartOfDay();
        LocalDateTime to = date == null ? LocalDateTime.of(2100, 1, 1, 0, 0) : date.plusDays(1).atStartOfDay();
        List<Booking> list = resourceId == null ? bookings.searchAll(from, to) : bookings.searchByResource(resourceId, from, to);
        return list.stream().map(BookingView::from).toList();
    }

    // Reminder alert 30 minutes before a booking starts
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();
        for (Booking b : bookings.findReminderDue(BookingStatus.CONFIRMED, now, now.plusMinutes(30))) {
            notifier.notify(b.user, "Booking reminder", b.resource.name + " starts at " + b.startTime + ".");
            b.reminderSent = true;
        }
    }

    private Booking owned(AuthUser me, Long id) {
        Booking b = bookings.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found."));
        if (!b.user.id.equals(me.id()) && me.role() != Role.ADMIN)
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only change your own bookings.");
        return b;
    }

    private void validate(Resource r, Role role, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Start and end time are required.");
        if (!end.isAfter(start)) throw new ApiException(HttpStatus.BAD_REQUEST, "End time must be after start time.");
        if (start.isBefore(LocalDateTime.now().minusMinutes(1))) throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot book a time in the past.");
        if (!r.availability) throw new ApiException(HttpStatus.BAD_REQUEST, r.name + " is not available for booking.");
        if (role == Role.STUDENT && "CLASSROOM".equalsIgnoreCase(r.type))
            throw new ApiException(HttpStatus.FORBIDDEN, "Students can book lockers, labs and equipment. Classrooms are for faculty.");
    }

    private void checkOverlap(AuthUser me, Resource r, LocalDateTime start, LocalDateTime end, Long excludeId) {
        if (bookings.existsOverlap(r.id, start, end, excludeId, BookingStatus.CONFIRMED)) {
            audit.log(me.id(), me.username(), "BOOKING_CONFLICT " + r.name + " " + start + " to " + end);
            throw new ApiException(HttpStatus.CONFLICT, r.name + " is already booked for that time. Pick another slot.");
        }
    }
}
