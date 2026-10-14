package com.gym.service;

import com.gym.dto.PaymentRequest;
import com.gym.dto.PaymentResponse;
import com.gym.entity.Member;
import com.gym.entity.Membership;
import com.gym.entity.Payment;
import com.gym.enums.PaymentStatus;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.MemberRepository;
import com.gym.repository.MembershipRepository;
import com.gym.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            MemberRepository memberRepository,
            MembershipRepository membershipRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.memberRepository = memberRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAll() {
        return paymentRepository.findAllWithDetails().stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getByMember(Long memberId) {
        return paymentRepository.findByMemberIdWithDetails(memberId).stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        YearMonth ym = YearMonth.now();
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(LocalTime.MAX);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue", paymentRepository.sumPaidAmount());
        stats.put("monthlyRevenue", paymentRepository.sumPaidAmountBetween(start, end));
        stats.put("pendingPayments", paymentRepository.countByStatus(PaymentStatus.PENDING));
        return stats;
    }

    @Transactional
    public PaymentResponse create(PaymentRequest request) {
        Member member = memberRepository.findByIdWithDetails(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        Payment payment = new Payment();
        payment.setMember(member);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH");
        payment.setTransactionReference(
                request.getTransactionReference() != null
                        ? request.getTransactionReference()
                        : "FC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
        );
        payment.setStatus(request.getStatus() != null
                ? PaymentStatus.valueOf(request.getStatus().toUpperCase())
                : PaymentStatus.PAID);
        payment.setPaymentDate(LocalDateTime.now());

        Long membershipId = request.getMembershipId();
        if (membershipId == null && member.getMembership() != null) {
            membershipId = member.getMembership().getId();
        }
        if (membershipId != null) {
            Membership membership = membershipRepository.findById(membershipId)
                    .orElseThrow(() -> new ResourceNotFoundException("Membership not found"));
            payment.setMembership(membership);
        }

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Transactional
    public PaymentResponse updateStatus(Long id, String status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        payment.setStatus(PaymentStatus.valueOf(status.toUpperCase()));
        paymentRepository.save(payment);
        return getAll().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(PaymentResponse.from(payment));
    }

    public BigDecimal monthlyRevenue() {
        YearMonth ym = YearMonth.now();
        return paymentRepository.sumPaidAmountBetween(
                ym.atDay(1).atStartOfDay(),
                ym.atEndOfMonth().atTime(LocalTime.MAX)
        );
    }
}
