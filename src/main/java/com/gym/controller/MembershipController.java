package com.gym.controller;

import com.gym.dto.MembershipRequest;
import com.gym.dto.MembershipResponse;
import com.gym.service.MembershipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/memberships")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping
    public ResponseEntity<List<MembershipResponse>> getAll() {
        return ResponseEntity.ok(membershipService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MembershipResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(membershipService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MembershipResponse> create(@Valid @RequestBody MembershipRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membershipService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MembershipResponse> update(@PathVariable Long id, @Valid @RequestBody MembershipRequest request) {
        return ResponseEntity.ok(membershipService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        membershipService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Membership deleted"));
    }

    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MembershipResponse> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(membershipService.toggleStatus(id));
    }
}
