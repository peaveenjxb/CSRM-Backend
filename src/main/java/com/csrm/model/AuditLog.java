package com.csrm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public Long userId;
    public String username;
    @Column(nullable = false, length = 500) public String action;
    @Column(nullable = false) public LocalDateTime timestamp;
}
