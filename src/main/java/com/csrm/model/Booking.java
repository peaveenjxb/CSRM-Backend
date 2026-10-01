package com.csrm.model;

import com.csrm.dto.Enums;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id") public User user;
    @ManyToOne(optional = false) @JoinColumn(name = "resource_id") public Resource resource;
    @Column(nullable = false) public LocalDateTime startTime;
    @Column(nullable = false) public LocalDateTime endTime;
    @Enumerated(EnumType.STRING) @Column(nullable = false) public Enums.BookingStatus status;
    public boolean reminderSent = false;
}
