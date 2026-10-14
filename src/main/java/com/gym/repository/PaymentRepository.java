package com.gym.repository;

import com.gym.entity.Payment;
import com.gym.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByMemberIdOrderByPaymentDateDesc(Long memberId);
    List<Payment> findByMembershipId(Long membershipId);
    List<Payment> findByStatus(PaymentStatus status);
    long countByStatus(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID'")
    BigDecimal sumPaidAmount();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' AND p.paymentDate BETWEEN :start AND :end")
    BigDecimal sumPaidAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT p FROM Payment p JOIN FETCH p.member m JOIN FETCH m.user LEFT JOIN FETCH p.membership ORDER BY p.paymentDate DESC")
    List<Payment> findAllWithDetails();

    @Query("SELECT p FROM Payment p JOIN FETCH p.member m JOIN FETCH m.user LEFT JOIN FETCH p.membership WHERE m.id = :memberId ORDER BY p.paymentDate DESC")
    List<Payment> findByMemberIdWithDetails(@Param("memberId") Long memberId);
}
