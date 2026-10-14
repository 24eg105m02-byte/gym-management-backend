package com.gym.service;

import com.gym.dto.MemberResponse;
import com.gym.dto.WorkoutPlanResponse;
import com.gym.entity.Member;
import com.gym.entity.Progress;
import com.gym.entity.Trainer;
import com.gym.enums.AttendanceStatus;
import com.gym.enums.EntityStatus;
import com.gym.enums.PaymentStatus;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;
    private final AttendanceRepository attendanceRepository;
    private final PaymentRepository paymentRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final ProgressRepository progressRepository;
    private final MembershipRepository membershipRepository;
    private final AttendanceService attendanceService;

    public DashboardService(
            MemberRepository memberRepository,
            TrainerRepository trainerRepository,
            AttendanceRepository attendanceRepository,
            PaymentRepository paymentRepository,
            WorkoutPlanRepository workoutPlanRepository,
            ProgressRepository progressRepository,
            MembershipRepository membershipRepository,
            AttendanceService attendanceService
    ) {
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
        this.attendanceRepository = attendanceRepository;
        this.paymentRepository = paymentRepository;
        this.workoutPlanRepository = workoutPlanRepository;
        this.progressRepository = progressRepository;
        this.membershipRepository = membershipRepository;
        this.attendanceService = attendanceService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminDashboard() {
        LocalDate today = LocalDate.now();
        YearMonth ym = YearMonth.now();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalMembers", memberRepository.count());
        data.put("activeTrainers", trainerRepository.countByStatus(EntityStatus.ACTIVE));
        data.put("todayAttendance", attendanceRepository.countByDate(today));
        data.put("monthlyRevenue", paymentRepository.sumPaidAmountBetween(
                ym.atDay(1).atStartOfDay(),
                ym.atEndOfMonth().atTime(LocalTime.MAX)
        ));

        List<MemberResponse> recent = memberRepository.findAllWithDetails().stream()
                .limit(5)
                .map(MemberResponse::from)
                .collect(Collectors.toList());
        data.put("recentMembers", recent);

        List<Map<String, Object>> weekly = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("day", d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            day.put("date", d.toString());
            day.put("count", attendanceRepository.countByDate(d));
            weekly.add(day);
        }
        data.put("weeklyAttendance", weekly);

        List<Map<String, Object>> revenue = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth m = ym.minusMonths(i);
            BigDecimal amount = paymentRepository.sumPaidAmountBetween(
                    m.atDay(1).atStartOfDay(),
                    m.atEndOfMonth().atTime(LocalTime.MAX)
            );
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", m.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            row.put("amount", amount);
            revenue.add(row);
        }
        data.put("revenueOverview", revenue);

        Map<String, Long> distribution = new LinkedHashMap<>();
        membershipRepository.findAll().forEach(ms -> {
            long count = memberRepository.findAllWithDetails().stream()
                    .filter(m -> m.getMembership() != null && m.getMembership().getId().equals(ms.getId()))
                    .count();
            distribution.put(ms.getName(), count);
        });
        data.put("membershipDistribution", distribution);
        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTrainerDashboard(Long userId) {
        Trainer trainer = trainerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer profile not found"));
        LocalDate today = LocalDate.now();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("assignedMembers", memberRepository.countByTrainerId(trainer.getId()));
        data.put("todayAttendance", attendanceRepository.findByTrainerIdAndDate(trainer.getId(), today).size());
        data.put("activeWorkoutPlans", workoutPlanRepository.countByTrainerIdAndStatus(trainer.getId(), EntityStatus.ACTIVE));

        Double avgWeight = progressRepository.averageWeightByTrainer(trainer.getId());
        data.put("averageProgress", avgWeight != null ? Math.round(avgWeight * 10.0) / 10.0 : 0);

        List<MemberResponse> recent = memberRepository.findByTrainerIdWithDetails(trainer.getId()).stream()
                .limit(5)
                .map(MemberResponse::from)
                .collect(Collectors.toList());
        data.put("recentMembers", recent);
        data.put("trainerId", trainer.getId());
        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMemberDashboard(Long userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member profile not found"));
        member = memberRepository.findByIdWithDetails(member.getId()).orElse(member);

        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> membership = new LinkedHashMap<>();
        if (member.getMembership() != null) {
            membership.put("name", member.getMembership().getName());
            membership.put("price", member.getMembership().getPrice());
            membership.put("status", member.getStatus().name());
            membership.put("startDate", member.getMembershipStartDate());
            membership.put("endDate", member.getMembershipEndDate());
            if (member.getMembershipEndDate() != null) {
                membership.put("remainingDays",
                        Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), member.getMembershipEndDate())));
            }
        }
        data.put("membership", membership);
        data.put("attendancePercentage", attendanceService.getAttendancePercentage(member.getId()));

        Optional<Progress> latest = progressRepository.findFirstByMemberIdOrderByRecordDateDesc(member.getId());
        data.put("currentWeight", latest.map(Progress::getWeight).orElse(null));

        var payments = paymentRepository.findByMemberIdWithDetails(member.getId());
        var nextPending = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .findFirst();
        data.put("nextPayment", nextPending.map(p -> Map.of(
                "amount", p.getAmount(),
                "status", p.getStatus().name(),
                "date", p.getPaymentDate()
        )).orElse(null));

        var workouts = workoutPlanRepository.findByMemberIdWithDetails(member.getId());
        if (!workouts.isEmpty()) {
            data.put("workoutSummary", WorkoutPlanResponse.from(workouts.get(0)));
        } else {
            data.put("workoutSummary", null);
        }

        data.put("memberId", member.getId());
        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getReports(LocalDate start, LocalDate end) {
        if (start == null) start = LocalDate.now().minusMonths(1);
        if (end == null) end = LocalDate.now();

        Map<String, Object> reports = new LinkedHashMap<>();

        List<Map<String, Object>> growth = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth m = YearMonth.now().minusMonths(i);
            long count = memberRepository.findAll().stream()
                    .filter(mem -> mem.getCreatedAt() != null
                            && !mem.getCreatedAt().isAfter(m.atEndOfMonth().atTime(LocalTime.MAX)))
                    .count();
            growth.add(Map.of("month", m.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH), "count", count));
        }
        reports.put("memberGrowth", growth);

        List<Map<String, Object>> attendance = new ArrayList<>();
        LocalDate d = start;
        while (!d.isAfter(end) && attendance.size() < 31) {
            attendance.add(Map.of(
                    "date", d.toString(),
                    "present", attendanceRepository.countByDateAndStatus(d, AttendanceStatus.PRESENT),
                    "late", attendanceRepository.countByDateAndStatus(d, AttendanceStatus.LATE),
                    "absent", attendanceRepository.countByDateAndStatus(d, AttendanceStatus.ABSENT)
            ));
            d = d.plusDays(1);
        }
        reports.put("attendanceReport", attendance);

        BigDecimal revenue = paymentRepository.sumPaidAmountBetween(start.atStartOfDay(), end.atTime(LocalTime.MAX));
        reports.put("revenueReport", Map.of(
                "total", revenue,
                "start", start.toString(),
                "end", end.toString()
        ));

        Map<String, Long> membershipReport = new LinkedHashMap<>();
        membershipRepository.findAll().forEach(ms -> {
            long count = memberRepository.findAllWithDetails().stream()
                    .filter(m -> m.getMembership() != null && m.getMembership().getId().equals(ms.getId()))
                    .count();
            membershipReport.put(ms.getName(), count);
        });
        reports.put("membershipReport", membershipReport);

        List<Map<String, Object>> trainerPerf = trainerRepository.findAllWithUser().stream()
                .map(t -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("name", t.getUser().getName());
                    row.put("members", memberRepository.countByTrainerId(t.getId()));
                    row.put("workouts", workoutPlanRepository.countByTrainerIdAndStatus(t.getId(), EntityStatus.ACTIVE));
                    return row;
                })
                .collect(Collectors.toList());
        reports.put("trainerPerformance", trainerPerf);

        return reports;
    }
}
