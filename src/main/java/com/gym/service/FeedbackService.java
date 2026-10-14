package com.gym.service;

import com.gym.dto.FeedbackRequest;
import com.gym.dto.FeedbackResponse;
import com.gym.entity.Feedback;
import com.gym.entity.Member;
import com.gym.entity.Trainer;
import com.gym.exception.BadRequestException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.FeedbackRepository;
import com.gym.repository.MemberRepository;
import com.gym.repository.TrainerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;

    public FeedbackService(
            FeedbackRepository feedbackRepository,
            MemberRepository memberRepository,
            TrainerRepository trainerRepository
    ) {
        this.feedbackRepository = feedbackRepository;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getAll() {
        return feedbackRepository.findAllWithDetails().stream()
                .map(FeedbackResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getByTrainer(Long trainerId) {
        return feedbackRepository.findByTrainerIdWithDetails(trainerId).stream()
                .map(FeedbackResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getByMember(Long memberId) {
        return feedbackRepository.findByMemberIdWithDetails(memberId).stream()
                .map(FeedbackResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public FeedbackResponse create(Long memberUserId, FeedbackRequest request) {
        Member member = memberRepository.findByUserIdWithDetails(memberUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member profile not found"));

        Long trainerId = request.getTrainerId();
        if (trainerId == null) {
            if (member.getTrainer() == null) {
                throw new BadRequestException("No trainer assigned");
            }
            trainerId = member.getTrainer().getId();
        }

        Trainer trainer = trainerRepository.findByIdWithUser(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));

        Feedback feedback = new Feedback();
        feedback.setMember(member);
        feedback.setTrainer(trainer);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        return FeedbackResponse.from(feedbackRepository.save(feedback));
    }
}
