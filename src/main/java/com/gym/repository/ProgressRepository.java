package com.gym.repository;

import com.gym.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
    List<Progress> findByMemberIdOrderByRecordDateDesc(Long memberId);

    @Query("SELECT p FROM Progress p JOIN FETCH p.member m JOIN FETCH m.user ORDER BY p.recordDate DESC")
    List<Progress> findAllWithDetails();

    @Query("SELECT p FROM Progress p JOIN FETCH p.member m JOIN FETCH m.user WHERE m.id = :memberId ORDER BY p.recordDate ASC")
    List<Progress> findByMemberIdWithDetails(@Param("memberId") Long memberId);

    @Query("SELECT p FROM Progress p JOIN FETCH p.member m JOIN FETCH m.user WHERE m.trainer.id = :trainerId ORDER BY p.recordDate DESC")
    List<Progress> findByTrainerIdWithDetails(@Param("trainerId") Long trainerId);

    Optional<Progress> findFirstByMemberIdOrderByRecordDateDesc(Long memberId);

    @Query("SELECT AVG(p.weight) FROM Progress p WHERE p.member.trainer.id = :trainerId")
    Double averageWeightByTrainer(@Param("trainerId") Long trainerId);
}
