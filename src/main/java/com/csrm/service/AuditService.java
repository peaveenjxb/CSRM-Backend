package com.csrm.service;

import com.csrm.model.AuditLog;
import com.csrm.repository.AuditLogRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final AuditLogRepository repo;
    public AuditService(AuditLogRepository repo) { this.repo = repo; }

    // Own transaction, so the entry is saved even when the calling request is rolled back (e.g. a booking conflict)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, String username, String action) {
        AuditLog a = new AuditLog();
        a.userId = userId;
        a.username = username;
        a.action = action.length() > 500 ? action.substring(0, 500) : action;
        a.timestamp = LocalDateTime.now();
        repo.save(a);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> find(Long userId, LocalDate date) {
        LocalDateTime from = date == null ? LocalDateTime.of(2000, 1, 1, 0, 0) : date.atStartOfDay();
        LocalDateTime to = date == null ? LocalDateTime.of(2100, 1, 1, 0, 0) : date.plusDays(1).atStartOfDay();
        return userId == null ? repo.findInRange(from, to) : repo.findByUserInRange(userId, from, to);
    }
}
