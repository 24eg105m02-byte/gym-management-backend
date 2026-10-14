package com.gym.controller;

import com.gym.dto.WorkoutPlanRequest;
import com.gym.dto.WorkoutPlanResponse;
import com.gym.enums.Role;
import com.gym.exception.BadRequestException;
import com.gym.repository.MemberRepository;
import com.gym.repository.TrainerRepository;
import com.gym.security.SecurityUtils;
import com.gym.service.WorkoutPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutPlanService workoutPlanService;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;

    public WorkoutController(
            WorkoutPlanService workoutPlanService,
            MemberRepository memberRepository,
            TrainerRepository trainerRepository
    ) {
        this.workoutPlanService = workoutPlanService;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER','MEMBER')")
    public ResponseEntity<List<WorkoutPlanResponse>> getAll() {
        var user = SecurityUtils.currentUser().getUser();
        if (user.getRole() == Role.MEMBER) {
            Long memberId = memberRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException("Member profile not found"))
                    .getId();
            return ResponseEntity.ok(workoutPlanService.getByMember(memberId));
        }
        if (user.getRole() == Role.TRAINER) {
            Long trainerId = trainerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException("Trainer profile not found"))
                    .getId();
            return ResponseEntity.ok(workoutPlanService.getByTrainer(trainerId));
        }
        return ResponseEntity.ok(workoutPlanService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER','MEMBER')")
    public ResponseEntity<WorkoutPlanResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(workoutPlanService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<WorkoutPlanResponse> create(@Valid @RequestBody WorkoutPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workoutPlanService.create(SecurityUtils.currentUserId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<WorkoutPlanResponse> update(@PathVariable Long id, @Valid @RequestBody WorkoutPlanRequest request) {
        return ResponseEntity.ok(workoutPlanService.update(id, SecurityUtils.currentUserId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        workoutPlanService.delete(id, SecurityUtils.currentUserId());
        return ResponseEntity.ok(Map.of("message", "Workout plan deleted"));
    }
}
