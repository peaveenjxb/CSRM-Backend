package com.csrm.service;

import com.csrm.dto.Dtos.UserUpdate;
import com.csrm.dto.Dtos.UserView;
import com.csrm.dto.Enums.BookingStatus;
import com.csrm.dto.Enums.Role;
import com.csrm.dto.Enums.UserStatus;
import com.csrm.exception.ApiException;
import com.csrm.model.Booking;
import com.csrm.model.Resource;
import com.csrm.model.User;
import com.csrm.repository.AuditLogRepository;
import com.csrm.repository.BookingRepository;
import com.csrm.repository.ResourceRepository;
import com.csrm.repository.UserRepository;
import com.csrm.security.AuthUser;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    private static final double DAY_HOURS = 10.0; // 08:00-18:00 bookable window

    private final UserRepository users;
    private final ResourceRepository resources;
    private final BookingRepository bookings;
    private final AuditLogRepository auditRepo;
    private final AuditService audit;
    private final NotificationService notifier;

    public AdminService(UserRepository users, ResourceRepository resources, BookingRepository bookings,
                        AuditLogRepository auditRepo, AuditService audit, NotificationService notifier) {
        this.users = users; this.resources = resources; this.bookings = bookings;
        this.auditRepo = auditRepo; this.audit = audit; this.notifier = notifier;
    }

    @Transactional(readOnly = true)
    public List<UserView> listUsers() { return users.findAll().stream().map(UserView::from).toList(); }

    @Transactional
    public UserView updateUser(Long id, UserUpdate up, AuthUser me) {
        User u = users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
        if (up.status() != null) {
            u.status = UserStatus.valueOf(up.status());
            audit.log(me.id(), me.username(), "USER_" + u.status + " " + u.username);
            notifier.notify(u, "Account " + u.status.name().toLowerCase(), "Your CSRM account is now " + u.status + ".");
        }
        if (up.role() != null) {
            if (u.id.equals(me.id())) throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot change your own role.");
            u.role = Role.valueOf(up.role());
            audit.log(me.id(), me.username(), "ROLE_CHANGED " + u.username + " -> " + u.role);
        }
        return UserView.from(u);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> stats() {
        LocalDate today = LocalDate.now();
        double booked = hoursBooked(bookings.searchAll(today.atStartOfDay(), today.plusDays(1).atStartOfDay()), today);
        double capacity = resources.count() * DAY_HOURS;
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("utilization", capacity == 0 ? 0 : Math.round(booked / capacity * 1000) / 10.0);
        m.put("pendingUsers", users.countByStatus(UserStatus.PENDING));
        m.put("activeBookings", bookings.countActive(BookingStatus.CONFIRMED, LocalDateTime.now()));
        m.put("conflicts", auditRepo.countByActionStartingWith("BOOKING_CONFLICT"));
        return m;
    }

    // Daily utilization per resource
    @Transactional(readOnly = true)
    public List<Map<String, Object>> utilizationReport(LocalDate date) {
        List<Booking> day = bookings.searchAll(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        List<Map<String, Object>> out = new ArrayList<>();
        for (Resource r : resources.findAll()) {
            double h = hoursBooked(day.stream().filter(b -> b.resource.id.equals(r.id)).toList(), date);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("resource", r.name);
            row.put("bookedHours", Math.round(h * 10) / 10.0);
            row.put("utilizationPercent", Math.round(h / DAY_HOURS * 1000) / 10.0);
            out.add(row);
        }
        return out;
    }


    private double hoursBooked(List<Booking> list, LocalDate day) {
        double total = 0;
        for (Booking b : list) {
            if (b.status != BookingStatus.CONFIRMED) continue;
            LocalDateTime s = b.startTime.isBefore(day.atStartOfDay()) ? day.atStartOfDay() : b.startTime;
            LocalDateTime e = b.endTime.isAfter(day.plusDays(1).atStartOfDay()) ? day.plusDays(1).atStartOfDay() : b.endTime;
            total += Duration.between(s, e).toMinutes() / 60.0;
        }
        return total;
    }
}
