package com.csrm.service;

import com.csrm.dto.Dtos.LoginReq;
import com.csrm.dto.Dtos.LoginRes;
import com.csrm.dto.Dtos.RegisterReq;
import com.csrm.dto.Enums;
import com.csrm.exception.ApiException;
import com.csrm.model.User;
import com.csrm.repository.UserRepository;
import com.csrm.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final AuditService audit;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt, AuditService audit) {
        this.users = users; this.encoder = encoder; this.jwt = jwt; this.audit = audit;
    }

    public LoginRes register(RegisterReq r) {
        String name = r.username().trim();
        if (users.existsByUsername(name)) throw new ApiException(HttpStatus.CONFLICT, "That username is already taken.");
        User u = new User();
        u.username = name;
        u.password = encoder.encode(r.password());
        u.email = r.email();
        u.role = "FACULTY".equalsIgnoreCase(r.role()) ? Enums.Role.FACULTY : Enums.Role.STUDENT; // ADMIN cannot self-register
        u.status = Enums.UserStatus.APPROVED;
        users.save(u);
        audit.log(u.id, u.username, "REGISTERED as " + u.role);
        return new LoginRes(jwt.generate(u), u.role.name(), u.username, u.id);
    }

    public LoginRes login(LoginReq r) {
        User u = users.findByUsername(r.username().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password."));
        if (!encoder.matches(r.password(), u.password))
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        if (u.status == Enums.UserStatus.PENDING)
            throw new ApiException(HttpStatus.FORBIDDEN, "Your account is waiting for admin approval.");
        if (u.status == Enums.UserStatus.REJECTED)
            throw new ApiException(HttpStatus.FORBIDDEN, "Your account request was rejected. Contact the administrator.");
        audit.log(u.id, u.username, "LOGIN");
        return new LoginRes(jwt.generate(u), u.role.name(), u.username, u.id);
    }
}

