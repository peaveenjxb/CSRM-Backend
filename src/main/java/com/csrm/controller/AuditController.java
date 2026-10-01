package com.csrm.controller;

import com.csrm.model.AuditLog;
import com.csrm.service.AuditService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
    private final AuditService audit;
    public AuditController(AuditService audit) { this.audit = audit; }

    @GetMapping
    public List<AuditLog> logs(@RequestParam(required = false) Long userId, @RequestParam(required = false) LocalDate date) {
        return audit.find(userId, date);
    }
}
