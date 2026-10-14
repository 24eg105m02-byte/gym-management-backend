package com.gym.service;

import com.gym.dto.AttendanceRequest;
import com.gym.dto.AttendanceResponse;
import com.gym.entity.Attendance;
import com.gym.entity.Member;
import com.gym.enums.AttendanceStatus;
import com.gym.exception.DuplicateResourceException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.AttendanceRepository;
import com.gym.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, MemberRepository memberRepository) {
        this.attendanceRepository = attendanceRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAll(Long memberId, LocalDate date) {
        if (memberId != null || date != null) {
            return attendanceRepository.findFiltered(memberId, date).stream()
                    .map(AttendanceResponse::from)
                    .collect(Collectors.toList());
        }
        return attendanceRepository.findFiltered(null, null).stream()
                .map(AttendanceResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getByTrainer(Long trainerId, LocalDate date) {
        return attendanceRepository.findByTrainerIdAndDate(trainerId, date).stream()
                .map(AttendanceResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getByMember(Long memberId) {
        return attendanceRepository.findFiltered(memberId, null).stream()
                .map(AttendanceResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTodayStats() {
        LocalDate today = LocalDate.now();
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayTotal", attendanceRepository.countByDate(today));
        stats.put("present", attendanceRepository.countByDateAndStatus(today, AttendanceStatus.PRESENT));
        stats.put("absent", attendanceRepository.countByDateAndStatus(today, AttendanceStatus.ABSENT));
        stats.put("late", attendanceRepository.countByDateAndStatus(today, AttendanceStatus.LATE));
        return stats;
    }

    @Transactional(readOnly = true)
    public double getAttendancePercentage(Long memberId) {
        long total = attendanceRepository.countByMemberId(memberId);
        if (total == 0) return 0;
        long present = attendanceRepository.countByMemberIdAndStatus(memberId, AttendanceStatus.PRESENT)
                + attendanceRepository.countByMemberIdAndStatus(memberId, AttendanceStatus.LATE);
        return Math.round((present * 1000.0 / total)) / 10.0;
    }

    @Transactional
    public AttendanceResponse create(AttendanceRequest request) {
        LocalDate date = request.getDate() != null ? request.getDate() : LocalDate.now();
        if (attendanceRepository.existsByMemberIdAndDate(request.getMemberId(), date)) {
            throw new DuplicateResourceException("Attendance already marked for this member on " + date);
        }
        Member member = memberRepository.findByIdWithDetails(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        Attendance a = new Attendance();
        a.setMember(member);
        a.setDate(date);
        a.setStatus(AttendanceStatus.valueOf(request.getStatus().toUpperCase()));
        a.setCheckInTime(request.getCheckInTime() != null ? request.getCheckInTime() : LocalTime.now());
        a.setCheckOutTime(request.getCheckOutTime());
        return AttendanceResponse.from(attendanceRepository.save(a));
    }

    @Transactional
    public AttendanceResponse update(Long id, AttendanceRequest request) {
        Attendance a = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        if (request.getStatus() != null) {
            a.setStatus(AttendanceStatus.valueOf(request.getStatus().toUpperCase()));
        }
        if (request.getCheckInTime() != null) a.setCheckInTime(request.getCheckInTime());
        if (request.getCheckOutTime() != null) a.setCheckOutTime(request.getCheckOutTime());
        if (request.getDate() != null) a.setDate(request.getDate());
        attendanceRepository.save(a);
        return AttendanceResponse.from(
                attendanceRepository.findFiltered(a.getMember().getId(), a.getDate()).stream()
                        .filter(x -> x.getId().equals(id))
                        .findFirst()
                        .orElse(a)
        );
    }
}
