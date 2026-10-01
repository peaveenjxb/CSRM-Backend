package com.csrm.dto;

import com.csrm.model.Booking;
import com.csrm.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class Dtos {
    public record RegisterReq(@NotBlank(message = "Username is required.") String username,
            @NotBlank(message = "Password is required.") @Size(min = 6, message = "Password must be at least 6 characters.") String password,
            String role,
            @NotBlank(message = "Email is required.") @Email(message = "Enter a valid email address.") String email) {
    }

    public record LoginReq(@NotBlank(message = "Username is required.") String username,
            @NotBlank(message = "Password is required.") String password) {
    }

    public record LoginRes(String token, String role, String username, Long id) {
    }

    public record UserUpdate(String status, String role) {
    }

    public record BookingReq(Long resourceId, LocalDateTime startTime, LocalDateTime endTime) {
    }

    public record ServiceReq(@NotBlank(message = "Please describe the service you need.") String description) {
    }

    // Never includes the password
    public record UserView(Long id, String username, String email, Enums.Role role, Enums.UserStatus status) {
        public static UserView from(User u) {
            return new UserView(u.id, u.username, u.email, u.role, u.status);
        }
    }

    public record BookingView(Long id, Long userId, String username, Long resourceId, String resourceName,
            LocalDateTime startTime, LocalDateTime endTime, Enums.BookingStatus status) {
        public static BookingView from(Booking b) {
            return new BookingView(b.id, b.user.id, b.user.username, b.resource.id, b.resource.name,
                    b.startTime, b.endTime, b.status);
        }
    }
}
