package com.gym.controller;

import com.gym.dto.AttendanceRequest;
import com.gym.dto.AttendanceResponse;
import com.gym.enums.Role;
import com.gym.exception.BadRequestException;
import com.gym.repository.MemberRepository;
import com.gym.repository.TrainerRepository;
import com.gym.security.SecurityUtils;
import com.gym.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;

    public AttendanceController(
            AttendanceService attendanceService,
            MemberRepository memberRepository,
            TrainerRepository trainerRepository
    ) {
        this.attendanceService = attendanceService;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER','MEMBER')")
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        var user = SecurityUtils.currentUser().getUser();
        if (user.getRole() == Role.MEMBER) {
            Long mid = memberRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException("Member profile not found"))
                    .getId();
            return ResponseEntity.ok(attendanceService.getByMember(mid));
        }
        if (user.getRole() == Role.TRAINER) {
            Long trainerId = trainerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException("Trainer profile not found"))
                    .getId();
            return ResponseEntity.ok(attendanceService.getByTrainer(trainerId, date));
        }
        return ResponseEntity.ok(attendanceService.getAll(memberId, date));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(attendanceService.getTodayStats());
    }

    @GetMapping("/percentage/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER','MEMBER')")
    public ResponseEntity<Map<String, Object>> percentage(@PathVariable Long memberId) {
        return ResponseEntity.ok(Map.of(
                "memberId", memberId,
                "percentage", attendanceService.getAttendancePercentage(memberId)
        ));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<AttendanceResponse> create(@Valid @RequestBody AttendanceRequest request) {
        if (SecurityUtils.currentUser().getUser().getRole() == Role.TRAINER) {
            Long trainerId = trainerRepository.findByUserId(SecurityUtils.currentUserId())
                    .orElseThrow(() -> new BadRequestException("Trainer profile not found"))
                    .getId();
            var member = memberRepository.findByIdWithDetails(request.getMemberId())
                    .orElseThrow(() -> new BadRequestException("Member not found"));
            if (member.getTrainer() == null || !member.getTrainer().getId().equals(trainerId)) {
                throw new BadRequestException("Member is not assigned to you");
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<AttendanceResponse> update(@PathVariable Long id, @RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.update(id, request));
    }
}
