package com.csrm.model;

import com.csrm.dto.Enums;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(unique = true, nullable = false) public String username;
    @Column(nullable = false) public String password; // BCrypt hash, never returned by the API
    public String email;
    @Enumerated(EnumType.STRING) @Column(nullable = false) public Enums.Role role;
    @Enumerated(EnumType.STRING) @Column(nullable = false) public Enums.UserStatus status;
}
