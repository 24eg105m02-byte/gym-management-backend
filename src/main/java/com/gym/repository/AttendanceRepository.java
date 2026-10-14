package com.gym.repository;

import com.gym.entity.Attendance;
import com.gym.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByMemberIdAndDate(Long memberId, LocalDate date);
    boolean existsByMemberIdAndDate(Long memberId, LocalDate date);
    List<Attendance> findByDate(LocalDate date);
    List<Attendance> findByMemberId(Long memberId);
    List<Attendance> findByMemberIdOrderByDateDesc(Long memberId);
    List<Attendance> findByDateBetween(LocalDate start, LocalDate end);
    long countByDateAndStatus(LocalDate date, AttendanceStatus status);
    long countByDate(LocalDate date);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.member m JOIN FETCH m.user LEFT JOIN FETCH m.trainer WHERE a.date = :date")
    List<Attendance> findByDateWithDetails(@Param("date") LocalDate date);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.member m JOIN FETCH m.user WHERE (:memberId IS NULL OR m.id = :memberId) AND (:date IS NULL OR a.date = :date) ORDER BY a.date DESC")
    List<Attendance> findFiltered(@Param("memberId") Long memberId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.member m JOIN FETCH m.user WHERE m.trainer.id = :trainerId AND (:date IS NULL OR a.date = :date) ORDER BY a.date DESC")
    List<Attendance> findByTrainerIdAndDate(@Param("trainerId") Long trainerId, @Param("date") LocalDate date);

    long countByMemberIdAndStatus(Long memberId, AttendanceStatus status);
    long countByMemberId(Long memberId);
}
