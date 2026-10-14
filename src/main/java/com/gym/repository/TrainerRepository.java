package com.gym.repository;

import com.gym.entity.Trainer;
import com.gym.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByUserId(Long userId);
    Optional<Trainer> findByUserEmail(String email);
    List<Trainer> findByStatus(EntityStatus status);
    long countByStatus(EntityStatus status);

    @Query("SELECT t FROM Trainer t JOIN FETCH t.user ORDER BY t.createdAt DESC")
    List<Trainer> findAllWithUser();

    @Query("SELECT t FROM Trainer t JOIN FETCH t.user WHERE t.id = :id")
    Optional<Trainer> findByIdWithUser(@Param("id") Long id);

    @Query("SELECT t FROM Trainer t JOIN FETCH t.user WHERE t.user.id = :userId")
    Optional<Trainer> findByUserIdWithUser(@Param("userId") Long userId);
}
