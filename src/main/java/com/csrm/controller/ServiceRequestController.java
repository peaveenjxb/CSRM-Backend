package com.csrm.controller;

import com.csrm.dto.Dtos.ServiceReq;
import com.csrm.security.AuthUser;
import com.csrm.service.AuditService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// Student service requests (e.g. lab assistance). Recorded in the audit log for admins to follow up.
@RestController
@RequestMapping("/api/services")
public class ServiceRequestController {
    private final AuditService audit;
    public ServiceRequestController(AuditService audit) { this.audit = audit; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> request(@Valid @RequestBody ServiceReq req, @AuthenticationPrincipal AuthUser me) {
        audit.log(me.id(), me.username(), "SERVICE_REQUEST " + req.description());
        return Map.of("message", "Service request sent.");
    }
}
