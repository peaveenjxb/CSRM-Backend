package com.csrm.security;

import com.csrm.dto.Enums;
import com.csrm.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${csrm.jwt.secret}") String secret, @Value("${csrm.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generate(User u) {
        return Jwts.builder().subject(u.username).claim("uid", u.id).claim("role", u.role.name())
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key).compact();
    }

    public AuthUser parse(String token) {
        Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return new AuthUser(((Number) c.get("uid")).longValue(), c.getSubject(), Enums.Role.valueOf((String) c.get("role")));
    }
}
