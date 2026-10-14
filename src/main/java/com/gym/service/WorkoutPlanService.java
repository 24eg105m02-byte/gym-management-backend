package com.gym.service;

import com.gym.dto.ExerciseRequest;
import com.gym.dto.WorkoutPlanRequest;
import com.gym.dto.WorkoutPlanResponse;
import com.gym.entity.Exercise;
import com.gym.entity.Member;
import com.gym.entity.Trainer;
import com.gym.entity.WorkoutPlan;
import com.gym.enums.EntityStatus;
import com.gym.exception.BadRequestException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.MemberRepository;
import com.gym.repository.TrainerRepository;
import com.gym.repository.WorkoutPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;

    public WorkoutPlanService(
            WorkoutPlanRepository workoutPlanRepository,
            MemberRepository memberRepository,
            TrainerRepository trainerRepository
    ) {
        this.workoutPlanRepository = workoutPlanRepository;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
    }

    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse> getAll() {
        return workoutPlanRepository.findAllWithDetails().stream()
                .map(WorkoutPlanResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse> getByTrainer(Long trainerId) {
        return workoutPlanRepository.findByTrainerIdWithDetails(trainerId).stream()
                .map(WorkoutPlanResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkoutPlanResponse> getByMember(Long memberId) {
        return workoutPlanRepository.findByMemberIdWithDetails(memberId).stream()
                .map(WorkoutPlanResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkoutPlanResponse getById(Long id) {
        return WorkoutPlanResponse.from(find(id));
    }

    @Transactional
    public WorkoutPlanResponse create(Long trainerUserId, WorkoutPlanRequest request) {
        Trainer trainer = trainerRepository.findByUserId(trainerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer profile not found"));
        Member member = memberRepository.findByIdWithDetails(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (member.getTrainer() == null || !member.getTrainer().getId().equals(trainer.getId())) {
            throw new BadRequestException("Member is not assigned to you");
        }

        WorkoutPlan plan = new WorkoutPlan();
        plan.setTrainer(trainer);
        plan.setMember(member);
        apply(plan, request);
        addExercises(plan, request.getExercises());
        return WorkoutPlanResponse.from(workoutPlanRepository.save(plan));
    }

    @Transactional
    public WorkoutPlanResponse update(Long id, Long trainerUserId, WorkoutPlanRequest request) {
        WorkoutPlan plan = find(id);
        Trainer trainer = trainerRepository.findByUserId(trainerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer profile not found"));
        if (!plan.getTrainer().getId().equals(trainer.getId())) {
            throw new BadRequestException("You can only edit your own workout plans");
        }
        if (request.getMemberId() != null && !request.getMemberId().equals(plan.getMember().getId())) {
            Member member = memberRepository.findByIdWithDetails(request.getMemberId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
            if (member.getTrainer() == null || !member.getTrainer().getId().equals(trainer.getId())) {
                throw new BadRequestException("Member is not assigned to you");
            }
            plan.setMember(member);
        }
        apply(plan, request);
        plan.clearExercises();
        addExercises(plan, request.getExercises());
        return WorkoutPlanResponse.from(workoutPlanRepository.save(plan));
    }

    @Transactional
    public void delete(Long id, Long trainerUserId) {
        WorkoutPlan plan = find(id);
        Trainer trainer = trainerRepository.findByUserId(trainerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer profile not found"));
        if (!plan.getTrainer().getId().equals(trainer.getId())) {
            throw new BadRequestException("You can only delete your own workout plans");
        }
        workoutPlanRepository.delete(plan);
    }

    private void apply(WorkoutPlan plan, WorkoutPlanRequest request) {
        if (request.getTitle() != null) plan.setTitle(request.getTitle());
        if (request.getGoal() != null) plan.setGoal(request.getGoal());
        if (request.getDescription() != null) plan.setDescription(request.getDescription());
        if (request.getStartDate() != null) plan.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) plan.setEndDate(request.getEndDate());
        if (request.getStatus() != null) {
            plan.setStatus(EntityStatus.valueOf(request.getStatus().toUpperCase()));
        } else if (plan.getStatus() == null) {
            plan.setStatus(EntityStatus.ACTIVE);
        }
    }

    private void addExercises(WorkoutPlan plan, List<ExerciseRequest> exercises) {
        if (exercises == null) return;
        for (ExerciseRequest er : exercises) {
            Exercise e = new Exercise();
            e.setName(er.getName());
            e.setSets(er.getSets());
            e.setReps(er.getReps());
            e.setWeight(er.getWeight());
            e.setRestTime(er.getRestTime());
            e.setNotes(er.getNotes());
            plan.addExercise(e);
        }
    }

    private WorkoutPlan find(Long id) {
        return workoutPlanRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workout plan not found"));
    }
}
