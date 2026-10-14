package com.gym.service;

import com.gym.dto.ProgressRequest;
import com.gym.dto.ProgressResponse;
import com.gym.entity.Member;
import com.gym.entity.Progress;
import com.gym.entity.Trainer;
import com.gym.exception.BadRequestException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.MemberRepository;
import com.gym.repository.ProgressRepository;
import com.gym.repository.TrainerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;

    public ProgressService(
            ProgressRepository progressRepository,
            MemberRepository memberRepository,
            TrainerRepository trainerRepository
    ) {
        this.progressRepository = progressRepository;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
    }

    @Transactional(readOnly = true)
    public List<ProgressResponse> getAll() {
        return progressRepository.findAllWithDetails().stream()
                .map(ProgressResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProgressResponse> getByMember(Long memberId) {
        return progressRepository.findByMemberIdWithDetails(memberId).stream()
                .map(ProgressResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProgressResponse> getByTrainer(Long trainerId) {
        return progressRepository.findByTrainerIdWithDetails(trainerId).stream()
                .map(ProgressResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProgressResponse create(Long trainerUserId, ProgressRequest request) {
        Trainer trainer = trainerRepository.findByUserId(trainerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer profile not found"));
        Member member = memberRepository.findByIdWithDetails(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (member.getTrainer() == null || !member.getTrainer().getId().equals(trainer.getId())) {
            throw new BadRequestException("Member is not assigned to you");
        }

        Progress p = new Progress();
        p.setMember(member);
        apply(p, request);
        return ProgressResponse.from(progressRepository.save(p));
    }

    @Transactional
    public ProgressResponse update(Long id, ProgressRequest request) {
        Progress p = progressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Progress record not found"));
        Member m = memberRepository.findByIdWithDetails(p.getMember().getId()).orElse(p.getMember());
        p.setMember(m);
        apply(p, request);
        return ProgressResponse.from(progressRepository.save(p));
    }

    private void apply(Progress p, ProgressRequest request) {
        if (request.getWeight() != null) p.setWeight(request.getWeight());
        if (request.getHeight() != null) p.setHeight(request.getHeight());
        if (request.getBodyFat() != null) p.setBodyFat(request.getBodyFat());
        if (request.getStrength() != null) p.setStrength(request.getStrength());
        if (request.getNotes() != null) p.setNotes(request.getNotes());
        if (request.getRecordDate() != null) {
            p.setRecordDate(request.getRecordDate());
        } else if (p.getRecordDate() == null) {
            p.setRecordDate(LocalDate.now());
        }
        if (request.getBmi() != null) {
            p.setBmi(request.getBmi());
        } else if (p.getWeight() != null && p.getHeight() != null && p.getHeight() > 0) {
            double h = p.getHeight() / 100.0;
            p.setBmi(Math.round((p.getWeight() / (h * h)) * 10.0) / 10.0);
        }
    }
}
