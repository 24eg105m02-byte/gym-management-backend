package com.gym.service;

import com.gym.dto.MemberRequest;
import com.gym.dto.MemberResponse;
import com.gym.entity.Member;
import com.gym.entity.Membership;
import com.gym.entity.Payment;
import com.gym.entity.Trainer;
import com.gym.entity.User;
import com.gym.enums.EntityStatus;
import com.gym.enums.PaymentStatus;
import com.gym.enums.Role;
import com.gym.enums.UserStatus;
import com.gym.exception.BadRequestException;
import com.gym.exception.DuplicateResourceException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final TrainerRepository trainerRepository;
    private final AttendanceRepository attendanceRepository;
    private final PaymentRepository paymentRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final ProgressRepository progressRepository;
    private final FeedbackRepository feedbackRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(
            MemberRepository memberRepository,
            UserRepository userRepository,
            MembershipRepository membershipRepository,
            TrainerRepository trainerRepository,
            AttendanceRepository attendanceRepository,
            PaymentRepository paymentRepository,
            WorkoutPlanRepository workoutPlanRepository,
            ProgressRepository progressRepository,
            FeedbackRepository feedbackRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.trainerRepository = trainerRepository;
        this.attendanceRepository = attendanceRepository;
        this.paymentRepository = paymentRepository;
        this.workoutPlanRepository = workoutPlanRepository;
        this.progressRepository = progressRepository;
        this.feedbackRepository = feedbackRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getAll() {
        return memberRepository.findAllWithDetails().stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MemberResponse getById(Long id) {
        return MemberResponse.from(findWithDetails(id));
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getByTrainerId(Long trainerId) {
        return memberRepository.findByTrainerIdWithDetails(trainerId).stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MemberResponse getByUserId(Long userId) {
        Member member = memberRepository.findByUserIdWithDetails(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member profile not found"));
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse create(MemberRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required for new members");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(Role.MEMBER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        Member member = new Member();
        member.setUser(user);
        applyMemberFields(member, request);
        user.setStatus(member.getStatus() == EntityStatus.INACTIVE ? UserStatus.INACTIVE : UserStatus.ACTIVE);
        userRepository.save(user);
        memberRepository.save(member);
        return MemberResponse.from(findWithDetails(member.getId()));
    }

    @Transactional
    public MemberResponse update(Long id, MemberRequest request) {
        Member member = findWithDetails(id);
        User user = member.getUser();

        if (request.getName() != null) {
            user.setName(request.getName().trim());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().toLowerCase().trim();
            if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new DuplicateResourceException("Email already registered");
            }
            user.setEmail(newEmail);
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        applyMemberFields(member, request);
        user.setStatus(member.getStatus() == EntityStatus.INACTIVE ? UserStatus.INACTIVE : UserStatus.ACTIVE);
        userRepository.save(user);
        memberRepository.save(member);
        return MemberResponse.from(findWithDetails(id));
    }

    @Transactional
    public MemberResponse updateOwnProfile(Long userId, MemberRequest request) {
        Member member = memberRepository.findByUserIdWithDetails(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member profile not found"));
        User user = member.getUser();
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getDateOfBirth() != null) {
            member.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            member.setGender(request.getGender());
        }
        if (request.getAddress() != null) {
            member.setAddress(request.getAddress());
        }
        if (request.getEmergencyContact() != null) {
            member.setEmergencyContact(request.getEmergencyContact());
        }
        if (request.getTrainerId() != null) {
            Trainer trainer = trainerRepository.findById(request.getTrainerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));
            member.setTrainer(trainer);
        }
        if (request.getMembershipId() != null) {
            Membership membership = membershipRepository.findById(request.getMembershipId())
                    .orElseThrow(() -> new ResourceNotFoundException("Membership not found"));
            member.setMembership(membership);
            LocalDate start = LocalDate.now();
            member.setMembershipStartDate(start);
            if (membership.getDuration() != null) {
                member.setMembershipEndDate(start.plusDays(membership.getDuration()));
            }
            member.setStatus(EntityStatus.ACTIVE);
            user.setStatus(UserStatus.ACTIVE);

            Payment payment = new Payment();
            payment.setMember(member);
            payment.setMembership(membership);
            payment.setAmount(membership.getPrice());
            payment.setPaymentDate(LocalDateTime.now());
            payment.setPaymentMethod("ONLINE");
            payment.setTransactionReference("FC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            payment.setStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);
        }
        userRepository.save(user);
        memberRepository.save(member);
        return MemberResponse.from(findWithDetails(member.getId()));
    }

    @Transactional
    public void delete(Long id) {
        Member member = memberRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        User user = member.getUser();

        attendanceRepository.deleteAll(attendanceRepository.findByMemberId(id));
        paymentRepository.deleteAll(paymentRepository.findByMemberIdOrderByPaymentDateDesc(id));
        workoutPlanRepository.deleteAll(workoutPlanRepository.findByMemberId(id));
        progressRepository.deleteAll(progressRepository.findByMemberIdOrderByRecordDateDesc(id));
        feedbackRepository.deleteAll(feedbackRepository.findByMemberIdOrderByCreatedAtDesc(id));

        memberRepository.delete(member);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Transactional
    public MemberResponse toggleStatus(Long id) {
        Member member = findWithDetails(id);
        EntityStatus next = member.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE;
        member.setStatus(next);
        member.getUser().setStatus(next == EntityStatus.ACTIVE ? UserStatus.ACTIVE : UserStatus.INACTIVE);
        userRepository.save(member.getUser());
        memberRepository.save(member);
        return MemberResponse.from(member);
    }

    private void applyMemberFields(Member member, MemberRequest request) {
        if (request.getDateOfBirth() != null) member.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) member.setGender(request.getGender());
        if (request.getAddress() != null) member.setAddress(request.getAddress());
        if (request.getEmergencyContact() != null) member.setEmergencyContact(request.getEmergencyContact());
        if (request.getMembershipStartDate() != null) member.setMembershipStartDate(request.getMembershipStartDate());
        if (request.getMembershipEndDate() != null) member.setMembershipEndDate(request.getMembershipEndDate());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            member.setStatus(EntityStatus.valueOf(request.getStatus().toUpperCase()));
        }

        if (request.getMembershipId() != null) {
            Membership membership = membershipRepository.findById(request.getMembershipId())
                    .orElseThrow(() -> new ResourceNotFoundException("Membership not found"));
            member.setMembership(membership);
            if (member.getMembershipStartDate() == null) {
                member.setMembershipStartDate(LocalDate.now());
            }
            if (member.getMembershipEndDate() == null && membership.getDuration() != null) {
                member.setMembershipEndDate(member.getMembershipStartDate().plusDays(membership.getDuration()));
            }
        }

        if (request.getTrainerId() != null) {
            Trainer trainer = trainerRepository.findById(request.getTrainerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trainer not found"));
            member.setTrainer(trainer);
        }
    }

    private Member findWithDetails(Long id) {
        return memberRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
    }
}
