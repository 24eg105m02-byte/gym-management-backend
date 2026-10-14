package com.gym.repository;

import com.gym.entity.WorkoutPlan;
import com.gym.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {
    List<WorkoutPlan> findByTrainerId(Long trainerId);
    List<WorkoutPlan> findByMemberId(Long memberId);
    long countByTrainerIdAndStatus(Long trainerId, EntityStatus status);

    @Query("SELECT DISTINCT w FROM WorkoutPlan w JOIN FETCH w.member m JOIN FETCH m.user JOIN FETCH w.trainer t JOIN FETCH t.user LEFT JOIN FETCH w.exercises ORDER BY w.createdAt DESC")
    List<WorkoutPlan> findAllWithDetails();

    @Query("SELECT DISTINCT w FROM WorkoutPlan w JOIN FETCH w.member m JOIN FETCH m.user JOIN FETCH w.trainer t JOIN FETCH t.user LEFT JOIN FETCH w.exercises WHERE w.trainer.id = :trainerId ORDER BY w.createdAt DESC")
    List<WorkoutPlan> findByTrainerIdWithDetails(@Param("trainerId") Long trainerId);

    @Query("SELECT DISTINCT w FROM WorkoutPlan w JOIN FETCH w.member m JOIN FETCH m.user JOIN FETCH w.trainer t JOIN FETCH t.user LEFT JOIN FETCH w.exercises WHERE w.member.id = :memberId ORDER BY w.createdAt DESC")
    List<WorkoutPlan> findByMemberIdWithDetails(@Param("memberId") Long memberId);

    @Query("SELECT DISTINCT w FROM WorkoutPlan w JOIN FETCH w.member m JOIN FETCH m.user JOIN FETCH w.trainer t JOIN FETCH t.user LEFT JOIN FETCH w.exercises WHERE w.id = :id")
    Optional<WorkoutPlan> findByIdWithDetails(@Param("id") Long id);
}
