package com.gym.repository;

import com.gym.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByTrainerIdOrderByCreatedAtDesc(Long trainerId);
    List<Feedback> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    @Query("SELECT f FROM Feedback f JOIN FETCH f.member m JOIN FETCH m.user JOIN FETCH f.trainer t JOIN FETCH t.user ORDER BY f.createdAt DESC")
    List<Feedback> findAllWithDetails();

    @Query("SELECT f FROM Feedback f JOIN FETCH f.member m JOIN FETCH m.user JOIN FETCH f.trainer t JOIN FETCH t.user WHERE t.id = :trainerId ORDER BY f.createdAt DESC")
    List<Feedback> findByTrainerIdWithDetails(@Param("trainerId") Long trainerId);

    @Query("SELECT f FROM Feedback f JOIN FETCH f.member m JOIN FETCH m.user JOIN FETCH f.trainer t JOIN FETCH t.user WHERE m.id = :memberId ORDER BY f.createdAt DESC")
    List<Feedback> findByMemberIdWithDetails(@Param("memberId") Long memberId);
}
