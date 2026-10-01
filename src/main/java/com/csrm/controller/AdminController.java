package com.csrm.controller;

import com.csrm.dto.Dtos.UserUpdate;
import com.csrm.dto.Dtos.UserView;
import com.csrm.security.AuthUser;
import com.csrm.service.AdminService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService admin;
    public AdminController(AdminService admin) { this.admin = admin; }

    @GetMapping("/users")
    public List<UserView> users() { return admin.listUsers(); }

    // Body: {"status":"APPROVED"|"REJECTED"} and/or {"role":"STUDENT"|"FACULTY"|"ADMIN"}
    @PutMapping("/users/{id}")
    public UserView update(@PathVariable Long id, @RequestBody UserUpdate body, @AuthenticationPrincipal AuthUser me) {
        return admin.updateUser(id, body, me);
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() { return admin.stats(); }

    @GetMapping("/reports/utilization")
    public List<Map<String, Object>> utilization(@RequestParam(required = false) LocalDate date) {
        return admin.utilizationReport(date == null ? LocalDate.now() : date);
    }
}
