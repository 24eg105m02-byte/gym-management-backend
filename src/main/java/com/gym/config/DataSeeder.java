package com.gym.config;

import com.gym.entity.*;
import com.gym.enums.*;
import com.gym.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final TrainerRepository trainerRepository;
    private final MemberRepository memberRepository;
    private final AttendanceRepository attendanceRepository;
    private final PaymentRepository paymentRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final ProgressRepository progressRepository;
    private final FeedbackRepository feedbackRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    public DataSeeder(
            UserRepository userRepository,
            MembershipRepository membershipRepository,
            TrainerRepository trainerRepository,
            MemberRepository memberRepository,
            AttendanceRepository attendanceRepository,
            PaymentRepository paymentRepository,
            WorkoutPlanRepository workoutPlanRepository,
            ProgressRepository progressRepository,
            FeedbackRepository feedbackRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.trainerRepository = trainerRepository;
        this.memberRepository = memberRepository;
        this.attendanceRepository = attendanceRepository;
        this.paymentRepository = paymentRepository;
        this.workoutPlanRepository = workoutPlanRepository;
        this.progressRepository = progressRepository;
        this.feedbackRepository = feedbackRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        if (!userRepository.existsByEmail("admin@fitcore.com")) {
            User admin = new User();
            admin.setName("FitCore Admin");
            admin.setEmail("admin@fitcore.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setPhone("9000000001");
            admin.setRole(Role.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            userRepository.save(admin);
        }

        Membership basic = ensureMembership("Basic", "Essential gym access", new BigDecimal("999"), 30,
                "Gym floor access, Locker room, Basic equipment");
        Membership premium = ensureMembership("Premium", "Gym + trainer support", new BigDecimal("1999"), 30,
                "Everything in Basic, Personal trainer sessions, Group classes");
        ensureMembership("Elite", "Full premium experience", new BigDecimal("2999"), 30,
                "Everything in Premium, Nutrition plan, Priority booking, Sauna access");

        Trainer trainer = trainerRepository.findByUserEmail("trainer@fitcore.com").orElseGet(() -> {
            User tu = new User();
            tu.setName("Rahul Sharma");
            tu.setEmail("trainer@fitcore.com");
            tu.setPassword(passwordEncoder.encode("trainer123"));
            tu.setPhone("9000000002");
            tu.setRole(Role.TRAINER);
            tu.setStatus(UserStatus.ACTIVE);
            userRepository.save(tu);

            Trainer t = new Trainer();
            t.setUser(tu);
            t.setSpecialization("Strength & Conditioning");
            t.setExperience(5);
            t.setSalary(new BigDecimal("35000"));
            t.setJoiningDate(LocalDate.now().minusMonths(6));
            t.setStatus(EntityStatus.ACTIVE);
            return trainerRepository.save(t);
        });

        Member member = memberRepository.findByUserEmail("member@fitcore.com").orElseGet(() -> {
            User mu = new User();
            mu.setName("Priya Patel");
            mu.setEmail("member@fitcore.com");
            mu.setPassword(passwordEncoder.encode("member123"));
            mu.setPhone("9000000003");
            mu.setRole(Role.MEMBER);
            mu.setStatus(UserStatus.ACTIVE);
            userRepository.save(mu);

            Member m = new Member();
            m.setUser(mu);
            m.setDateOfBirth(LocalDate.of(1998, 5, 12));
            m.setGender("Female");
            m.setAddress("Hyderabad, India");
            m.setEmergencyContact("9000000099");
            m.setMembership(premium);
            m.setTrainer(trainer);
            m.setMembershipStartDate(LocalDate.now().minusDays(15));
            m.setMembershipEndDate(LocalDate.now().plusDays(15));
            m.setStatus(EntityStatus.ACTIVE);
            return memberRepository.save(m);
        });

        if (attendanceRepository.count() == 0) {
            for (int i = 0; i < 7; i++) {
                Attendance a = new Attendance();
                a.setMember(member);
                a.setDate(LocalDate.now().minusDays(i));
                a.setStatus(i % 3 == 0 ? AttendanceStatus.LATE : AttendanceStatus.PRESENT);
                a.setCheckInTime(LocalTime.of(7, 30));
                a.setCheckOutTime(LocalTime.of(9, 0));
                attendanceRepository.save(a);
            }
        }

        if (paymentRepository.count() == 0) {
            Payment p = new Payment();
            p.setMember(member);
            p.setMembership(premium);
            p.setAmount(premium.getPrice());
            p.setPaymentDate(LocalDateTime.now().minusDays(15));
            p.setPaymentMethod("UPI");
            p.setTransactionReference("FC-PAY-001");
            p.setStatus(PaymentStatus.PAID);
            paymentRepository.save(p);
        }

        if (workoutPlanRepository.count() == 0) {
            WorkoutPlan plan = new WorkoutPlan();
            plan.setMember(member);
            plan.setTrainer(trainer);
            plan.setTitle("Beginner Strength Plan");
            plan.setGoal("Build muscle & endurance");
            plan.setDescription("4-week beginner program focusing on compound lifts.");
            plan.setStartDate(LocalDate.now().minusDays(10));
            plan.setEndDate(LocalDate.now().plusDays(20));
            plan.setStatus(EntityStatus.ACTIVE);

            Exercise e1 = new Exercise();
            e1.setName("Barbell Squat");
            e1.setSets(4);
            e1.setReps(8);
            e1.setWeight(40.0);
            e1.setRestTime("90s");
            e1.setNotes("Keep back straight");
            plan.addExercise(e1);

            Exercise e2 = new Exercise();
            e2.setName("Bench Press");
            e2.setSets(3);
            e2.setReps(10);
            e2.setWeight(30.0);
            e2.setRestTime("60s");
            plan.addExercise(e2);

            workoutPlanRepository.save(plan);
        }

        if (progressRepository.count() == 0) {
            for (int i = 4; i >= 0; i--) {
                Progress prog = new Progress();
                prog.setMember(member);
                prog.setWeight(62.0 + i);
                prog.setHeight(165.0);
                prog.setBodyFat(24.0 - i * 0.4);
                prog.setStrength(40.0 + i * 2);
                prog.setNotes("Weekly check-in");
                prog.setRecordDate(LocalDate.now().minusWeeks(i));
                progressRepository.save(prog);
            }
        }

        if (feedbackRepository.count() == 0) {
            Feedback fb = new Feedback();
            fb.setMember(member);
            fb.setTrainer(trainer);
            fb.setRating(5);
            fb.setComment("Great coaching and motivation!");
            feedbackRepository.save(fb);
        }
    }

    private Membership ensureMembership(String name, String desc, BigDecimal price, int duration, String features) {
        return membershipRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Membership m = new Membership();
            m.setName(name);
            m.setDescription(desc);
            m.setPrice(price);
            m.setDuration(duration);
            m.setFeatures(features);
            m.setStatus(EntityStatus.ACTIVE);
            return membershipRepository.save(m);
        });
    }
}
