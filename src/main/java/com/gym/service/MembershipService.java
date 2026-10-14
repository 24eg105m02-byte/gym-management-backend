package com.gym.service;

import com.gym.dto.MembershipRequest;
import com.gym.dto.MembershipResponse;
import com.gym.entity.Member;
import com.gym.entity.Membership;
import com.gym.entity.Payment;
import com.gym.enums.EntityStatus;
import com.gym.exception.DuplicateResourceException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.MemberRepository;
import com.gym.repository.MembershipRepository;
import com.gym.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    public MembershipService(
            MembershipRepository membershipRepository,
            MemberRepository memberRepository,
            PaymentRepository paymentRepository
    ) {
        this.membershipRepository = membershipRepository;
        this.memberRepository = memberRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public List<MembershipResponse> getAll() {
        return membershipRepository.findAll().stream()
                .map(MembershipResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MembershipResponse> getActive() {
        return membershipRepository.findByStatus(EntityStatus.ACTIVE).stream()
                .map(MembershipResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MembershipResponse getById(Long id) {
        return MembershipResponse.from(find(id));
    }

    @Transactional
    public MembershipResponse create(MembershipRequest request) {
        if (membershipRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Membership plan already exists");
        }
        Membership m = new Membership();
        apply(m, request);
        return MembershipResponse.from(membershipRepository.save(m));
    }

    @Transactional
    public MembershipResponse update(Long id, MembershipRequest request) {
        Membership m = find(id);
        apply(m, request);
        return MembershipResponse.from(membershipRepository.save(m));
    }

    @Transactional
    public void delete(Long id) {
        Membership m = find(id);
        List<Member> members = memberRepository.findByMembershipId(id);
        for (Member member : members) {
            member.setMembership(null);
        }
        memberRepository.saveAll(members);

        List<Payment> payments = paymentRepository.findByMembershipId(id);
        for (Payment payment : payments) {
            payment.setMembership(null);
        }
        paymentRepository.saveAll(payments);

        membershipRepository.delete(m);
    }

    @Transactional
    public MembershipResponse toggleStatus(Long id) {
        Membership m = find(id);
        m.setStatus(m.getStatus() == EntityStatus.ACTIVE ? EntityStatus.INACTIVE : EntityStatus.ACTIVE);
        return MembershipResponse.from(membershipRepository.save(m));
    }

    private void apply(Membership m, MembershipRequest request) {
        if (request.getName() != null) m.setName(request.getName().trim());
        if (request.getDescription() != null) m.setDescription(request.getDescription());
        if (request.getPrice() != null) m.setPrice(request.getPrice());
        if (request.getDuration() != null) m.setDuration(request.getDuration());
        if (request.getFeatures() != null) m.setFeatures(request.getFeatures());
        if (request.getStatus() != null) {
            m.setStatus(EntityStatus.valueOf(request.getStatus().toUpperCase()));
        } else if (m.getStatus() == null) {
            m.setStatus(EntityStatus.ACTIVE);
        }
    }

    private Membership find(Long id) {
        return membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found"));
    }
}
