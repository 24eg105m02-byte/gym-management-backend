package com.gym.controller;

import com.gym.dto.ProgressRequest;
import com.gym.dto.ProgressResponse;
import com.gym.enums.Role;
import com.gym.exception.BadRequestException;
import com.gym.repository.MemberRepository;
import com.gym.repository.TrainerRepository;
import com.gym.security.SecurityUtils;
import com.gym.service.ProgressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;

    public ProgressController(
            ProgressService progressService,
            MemberRepository memberRepository,
            TrainerRepository trainerRepository
    ) {
        this.progressService = progressService;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER','MEMBER')")
    public ResponseEntity<List<ProgressResponse>> getAll(@RequestParam(required = false) Long memberId) {
        var user = SecurityUtils.currentUser().getUser();
        if (user.getRole() == Role.MEMBER) {
            Long mid = memberRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException("Member profile not found"))
                    .getId();
            return ResponseEntity.ok(progressService.getByMember(mid));
        }
        if (user.getRole() == Role.TRAINER) {
            if (memberId != null) {
                return ResponseEntity.ok(progressService.getByMember(memberId));
            }
            Long trainerId = trainerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException("Trainer profile not found"))
                    .getId();
            return ResponseEntity.ok(progressService.getByTrainer(trainerId));
        }
        if (memberId != null) {
            return ResponseEntity.ok(progressService.getByMember(memberId));
        }
        return ResponseEntity.ok(progressService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<ProgressResponse> create(@Valid @RequestBody ProgressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(progressService.create(SecurityUtils.currentUserId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<ProgressResponse> update(@PathVariable Long id, @RequestBody ProgressRequest request) {
        return ResponseEntity.ok(progressService.update(id, request));
    }
}
