package com.csrm.controller;

import com.csrm.dto.Dtos.BookingReq;
import com.csrm.dto.Dtos.BookingView;
import com.csrm.security.AuthUser;
import com.csrm.service.BookingService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;
    public BookingController(BookingService service) { this.service = service; }

    @GetMapping("/my")
    public List<BookingView> mine(@AuthenticationPrincipal AuthUser me) { return service.mine(me); }

    @GetMapping
    public List<BookingView> search(@AuthenticationPrincipal AuthUser me,
                                    @RequestParam(required = false) Long resourceId,
                                    @RequestParam(required = false) LocalDate date) {
        return service.search(me, resourceId, date);
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public BookingView create(@AuthenticationPrincipal AuthUser me, @RequestBody BookingReq req) { return service.create(me, req); }

    @PutMapping("/{id}")
    public BookingView update(@AuthenticationPrincipal AuthUser me, @PathVariable Long id, @RequestBody BookingReq req) {
        return service.update(me, id, req);
    }

    // Cancelling keeps the row (status CANCELLED) so the audit trail stays complete
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@AuthenticationPrincipal AuthUser me, @PathVariable Long id) { service.cancel(me, id); }
}
