package com.gym.controller;

import com.gym.security.SecurityUtils;
import com.gym.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> adminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/admin/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> reports(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(dashboardService.getReports(start, end));
    }

    @GetMapping("/trainer/dashboard")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<Map<String, Object>> trainerDashboard() {
        return ResponseEntity.ok(dashboardService.getTrainerDashboard(SecurityUtils.currentUserId()));
    }

    @GetMapping("/member/dashboard")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<Map<String, Object>> memberDashboard() {
        return ResponseEntity.ok(dashboardService.getMemberDashboard(SecurityUtils.currentUserId()));
    }
}
