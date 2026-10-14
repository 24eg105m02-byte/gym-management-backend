package com.gym.repository;

import com.gym.entity.Member;
import com.gym.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserId(Long userId);
    Optional<Member> findByUserEmail(String email);
    List<Member> findByTrainerId(Long trainerId);
    List<Member> findByMembershipId(Long membershipId);
    List<Member> findByStatus(EntityStatus status);
    long countByStatus(EntityStatus status);
    long countByTrainerId(Long trainerId);

    @Query("SELECT m FROM Member m JOIN FETCH m.user LEFT JOIN FETCH m.membership LEFT JOIN FETCH m.trainer t LEFT JOIN FETCH t.user ORDER BY m.createdAt DESC")
    List<Member> findAllWithDetails();

    @Query("SELECT m FROM Member m JOIN FETCH m.user LEFT JOIN FETCH m.membership LEFT JOIN FETCH m.trainer t LEFT JOIN FETCH t.user WHERE m.id = :id")
    Optional<Member> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT m FROM Member m JOIN FETCH m.user LEFT JOIN FETCH m.membership LEFT JOIN FETCH m.trainer t LEFT JOIN FETCH t.user WHERE m.user.id = :userId")
    Optional<Member> findByUserIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT m FROM Member m JOIN FETCH m.user LEFT JOIN FETCH m.membership LEFT JOIN FETCH m.trainer t LEFT JOIN FETCH t.user WHERE m.trainer.id = :trainerId")
    List<Member> findByTrainerIdWithDetails(@Param("trainerId") Long trainerId);
}
