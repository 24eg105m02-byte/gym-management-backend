package com.gym.service;

import com.gym.dto.TrainerRequest;
import com.gym.dto.TrainerResponse;
import com.gym.entity.Member;
import com.gym.entity.Trainer;
import com.gym.entity.User;
import com.gym.enums.EntityStatus;
import com.gym.enums.Role;
import com.gym.enums.UserStatus;
import com.gym.exception.BadRequestException;
import com.gym.exception.DuplicateResourceException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final FeedbackRepository feedbackRepository;
    private final PasswordEncoder passwordEncoder;

    public TrainerService(
            TrainerRepository trainerRepository,
            UserRepository userRepository,
            MemberRepository memberRepository,
            WorkoutPlanRepository workoutPlanRepository,
            FeedbackRepository feedbackRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.workoutPlanRepository = workoutPlanRepository;
        this.feedbackRepository = feedbackRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<TrainerResponse> getAll() {
        return trainerRepository.findAllWithUser().stream()
                .map(t -> TrainerResponse.from(t, memberRepository.countByTrainerId(t.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TrainerResponse getById(Long id) {
        Trainer t = findWithUser(id);
        return TrainerResponse.from(t, memberRepository.countByTrainerId(t.getId()));
    }

    @Transactional(readOnly = true)
    public TrainerResponse getByUserId(Long userId) {
        Trainer t = trainerRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer profile not found"));
        return TrainerResponse.from(t, memberRepository.countByTrainerId(t.getId()));
    }

    @Transactional
    public TrainerResponse create(TrainerRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required for new trainers");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(Role.TRAINER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        applyFields(trainer, request);
        user.setStatus(trainer.getStatus() == EntityStatus.INACTIVE ? UserStatus.INACTIVE : UserStatus.ACTIVE);
        userRepository.save(user);
        trainerRepository.save(trainer);
        return TrainerResponse.from(findWithUser(trainer.getId()), 0);
    }

    @Transactional
    public TrainerResponse update(Long id, TrainerRequest request) {
        Trainer trainer = findWithUser(id);
        User user = trainer.getUser();
        if (request.getName() != null) user.setName(request.getName().trim());
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().toLowerCase().trim();
            if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new DuplicateResourceException("Email already registered");
            }
            user.setEmail(newEmail);
        }
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        applyFields(trainer, request);
        user.setStatus(trainer.getStatus() == EntityStatus.INACTIVE ? UserStatus.INACTIVE : UserStatus.ACTIVE);
        userRepository.save(user);
        trainerRepository.save(trainer);
        return TrainerResponse.from(findWithUser(id), memberRepository.countByTrainerId(id));
    }

    @Transactional
    public TrainerResponse updateOwnProfile(Long userId, TrainerRequest request) {
        Trainer trainer = trainerRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer profile not found"));
        User user = trainer.getUser();
        if (request.getName() != null && !request.getName().isBlank()) user.setName(request.getName().trim());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getSpecialization() != null) trainer.setSpecialization(request.getSpecialization());
        if (request.getExperience() != null) trainer.setExperience(request.getExperience());
        userRepository.save(user);
        trainerRepository.save(trainer);
        return getById(trainer.getId());
    }

    @Transactional
    public void delete(Long id) {
        Trainer trainer = trainerRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));
        User user = trainer.getUser();

        List<Member> assignedMembers = memberRepository.findByTrainerId(id);
        for (Member m : assignedMembers) {
            m.setTrainer(null);
        }
        memberRepository.saveAll(assignedMembers);

        workoutPlanRepository.deleteAll(workoutPlanRepository.findByTrainerId(id));
        feedbackRepository.deleteAll(feedbackRepository.findByTrainerIdOrderByCreatedAtDesc(id));

        trainerRepository.delete(trainer);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Transactional
    public TrainerResponse toggleStatus(Long id) {
        Trainer trainer = findWithUser(id);
        EntityStatus next = trainer.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE;
        trainer.setStatus(next);
        trainer.getUser().setStatus(next == EntityStatus.ACTIVE ? UserStatus.ACTIVE : UserStatus.INACTIVE);
        userRepository.save(trainer.getUser());
        trainerRepository.save(trainer);
        return TrainerResponse.from(trainer, memberRepository.countByTrainerId(id));
    }

    private void applyFields(Trainer trainer, TrainerRequest request) {
        if (request.getSpecialization() != null) trainer.setSpecialization(request.getSpecialization());
        if (request.getExperience() != null) trainer.setExperience(request.getExperience());
        if (request.getSalary() != null) trainer.setSalary(request.getSalary());
        if (request.getJoiningDate() != null) trainer.setJoiningDate(request.getJoiningDate());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            trainer.setStatus(EntityStatus.valueOf(request.getStatus().toUpperCase()));
        }
    }

    private Trainer findWithUser(Long id) {
        return trainerRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));
    }
}
