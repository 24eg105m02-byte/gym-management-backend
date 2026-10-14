package com.gym.controller;

import com.gym.dto.TrainerRequest;
import com.gym.dto.TrainerResponse;
import com.gym.enums.Role;
import com.gym.exception.BadRequestException;
import com.gym.repository.MemberRepository;
import com.gym.security.SecurityUtils;
import com.gym.service.TrainerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    private final TrainerService trainerService;
    private final MemberRepository memberRepository;

    public TrainerController(TrainerService trainerService, MemberRepository memberRepository) {
        this.trainerService = trainerService;
        this.memberRepository = memberRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER','MEMBER')")
    public ResponseEntity<List<TrainerResponse>> getAll() {
        return ResponseEntity.ok(trainerService.getAll());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<TrainerResponse> getMe() {
        return ResponseEntity.ok(trainerService.getByUserId(SecurityUtils.currentUserId()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<TrainerResponse> updateMe(@RequestBody TrainerRequest request) {
        return ResponseEntity.ok(trainerService.updateOwnProfile(SecurityUtils.currentUserId(), request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','TRAINER')")
    public ResponseEntity<TrainerResponse> getById(@PathVariable Long id) {
        var user = SecurityUtils.currentUser().getUser();
        if (user.getRole() == Role.MEMBER) {
            var member = memberRepository.findByUserIdWithDetails(user.getId())
                    .orElseThrow(() -> new BadRequestException("Member profile not found"));
            if (member.getTrainer() == null || !member.getTrainer().getId().equals(id)) {
                throw new BadRequestException("Access denied");
            }
        } else if (user.getRole() == Role.TRAINER) {
            var mine = trainerService.getByUserId(user.getId());
            if (!mine.getId().equals(id)) {
                throw new BadRequestException("Access denied");
            }
        }
        return ResponseEntity.ok(trainerService.getById(id));
    }

    @GetMapping("/my-trainer")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<?> getMyTrainer() {
        var member = memberRepository.findByUserIdWithDetails(SecurityUtils.currentUserId())
                .orElseThrow(() -> new BadRequestException("Member profile not found"));
        if (member.getTrainer() == null) {
            return ResponseEntity.ok(Map.of("message", "No trainer assigned"));
        }
        return ResponseEntity.ok(trainerService.getById(member.getTrainer().getId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainerResponse> create(@Valid @RequestBody TrainerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainerService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainerResponse> update(@PathVariable Long id, @Valid @RequestBody TrainerRequest request) {
        return ResponseEntity.ok(trainerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        trainerService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Trainer deleted"));
    }

    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainerResponse> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(trainerService.toggleStatus(id));
    }
}
