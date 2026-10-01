package com.csrm.controller;

import com.csrm.dto.Dtos.LoginReq;
import com.csrm.dto.Dtos.LoginRes;
import com.csrm.dto.Dtos.RegisterReq;
import com.csrm.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterReq req) {
        auth.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Registered. Waiting for admin approval."));
    }

    @PostMapping("/login")
    public LoginRes login(@Valid @RequestBody LoginReq req) { return auth.login(req); }
}
