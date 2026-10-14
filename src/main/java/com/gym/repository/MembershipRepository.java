package com.gym.repository;

import com.gym.entity.Membership;
import com.gym.enums.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByStatus(EntityStatus status);
    Optional<Membership> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
