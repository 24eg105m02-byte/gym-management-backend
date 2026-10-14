package com.gym.controller;

import com.gym.dto.FeedbackRequest;
import com.gym.dto.FeedbackResponse;
import com.gym.enums.Role;
import com.gym.exception.BadRequestException;
import com.gym.repository.MemberRepository;
import com.gym.repository.TrainerRepository;
import com.gym.security.SecurityUtils;
import com.gym.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final TrainerRepository trainerRepository;
    private final MemberRepository memberRepository;

    public FeedbackController(
            FeedbackService feedbackService,
            TrainerRepository trainerRepository,
            MemberRepository memberRepository
    ) {
        this.feedbackService = feedbackService;
        this.trainerRepository = trainerRepository;
        this.memberRepository = memberRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER','MEMBER')")
    public ResponseEntity<List<FeedbackResponse>> getAll() {
        var user = SecurityUtils.currentUser().getUser();
        if (user.getRole() == Role.TRAINER) {
            Long trainerId = trainerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException("Trainer profile not found"))
                    .getId();
            return ResponseEntity.ok(feedbackService.getByTrainer(trainerId));
        }
        if (user.getRole() == Role.MEMBER) {
            Long memberId = memberRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException("Member profile not found"))
                    .getId();
            return ResponseEntity.ok(feedbackService.getByMember(memberId));
        }
        return ResponseEntity.ok(feedbackService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<FeedbackResponse> create(@Valid @RequestBody FeedbackRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedbackService.create(SecurityUtils.currentUserId(), request));
    }
}
