package com.gym.controller;

import com.gym.dto.MemberRequest;
import com.gym.dto.MemberResponse;
import com.gym.enums.Role;
import com.gym.exception.BadRequestException;
import com.gym.repository.MemberRepository;
import com.gym.repository.TrainerRepository;
import com.gym.security.SecurityUtils;
import com.gym.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;

    public MemberController(
            MemberService memberService,
            MemberRepository memberRepository,
            TrainerRepository trainerRepository
    ) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<List<MemberResponse>> getAll() {
        var user = SecurityUtils.currentUser().getUser();
        if (user.getRole() == Role.TRAINER) {
            Long trainerId = trainerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException("Trainer profile not found"))
                    .getId();
            return ResponseEntity.ok(memberService.getByTrainerId(trainerId));
        }
        return ResponseEntity.ok(memberService.getAll());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<MemberResponse> getMe() {
        return ResponseEntity.ok(memberService.getByUserId(SecurityUtils.currentUserId()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<MemberResponse> updateMe(@RequestBody MemberRequest request) {
        return ResponseEntity.ok(memberService.updateOwnProfile(SecurityUtils.currentUserId(), request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER','MEMBER')")
    public ResponseEntity<MemberResponse> getById(@PathVariable Long id) {
        var user = SecurityUtils.currentUser().getUser();
        if (user.getRole() == Role.MEMBER) {
            var own = memberRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException("Member profile not found"));
            if (!own.getId().equals(id)) {
                throw new BadRequestException("Access denied");
            }
        } else if (user.getRole() == Role.TRAINER) {
            var member = memberService.getById(id);
            Long trainerId = trainerRepository.findByUserId(user.getId())
                    .map(t -> t.getId()).orElse(-1L);
            if (member.getTrainerId() == null || !member.getTrainerId().equals(trainerId)) {
                throw new BadRequestException("Access denied");
            }
            return ResponseEntity.ok(member);
        }
        return ResponseEntity.ok(memberService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberResponse> update(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(memberService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Member deleted"));
    }

    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberResponse> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.toggleStatus(id));
    }
}
