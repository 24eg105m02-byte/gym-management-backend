package com.gym.controller;

import com.gym.dto.PaymentRequest;
import com.gym.dto.PaymentResponse;
import com.gym.enums.Role;
import com.gym.exception.BadRequestException;
import com.gym.repository.MemberRepository;
import com.gym.security.SecurityUtils;
import com.gym.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final MemberRepository memberRepository;

    public PaymentController(PaymentService paymentService, MemberRepository memberRepository) {
        this.paymentService = paymentService;
        this.memberRepository = memberRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public ResponseEntity<List<PaymentResponse>> getAll() {
        var user = SecurityUtils.currentUser().getUser();
        if (user.getRole() == Role.MEMBER) {
            Long memberId = memberRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException("Member profile not found"))
                    .getId();
            return ResponseEntity.ok(paymentService.getByMember(memberId));
        }
        return ResponseEntity.ok(paymentService.getAll());
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(paymentService.getStats());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.create(request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(paymentService.updateStatus(id, body.get("status")));
    }
}
