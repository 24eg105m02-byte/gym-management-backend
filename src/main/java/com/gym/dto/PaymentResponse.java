package com.gym.dto;

import com.gym.entity.Payment;
import com.gym.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long membershipId;
    private String membershipName;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String paymentMethod;
    private String transactionReference;
    private PaymentStatus status;

    public static PaymentResponse from(Payment p) {
        PaymentResponse r = new PaymentResponse();
        r.id = p.getId();
        if (p.getMember() != null) {
            r.memberId = p.getMember().getId();
            if (p.getMember().getUser() != null) {
                r.memberName = p.getMember().getUser().getName();
            }
        }
        if (p.getMembership() != null) {
            r.membershipId = p.getMembership().getId();
            r.membershipName = p.getMembership().getName();
        }
        r.amount = p.getAmount();
        r.paymentDate = p.getPaymentDate();
        r.paymentMethod = p.getPaymentMethod();
        r.transactionReference = p.getTransactionReference();
        r.status = p.getStatus();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public Long getMembershipId() { return membershipId; }
    public void setMembershipId(Long membershipId) { this.membershipId = membershipId; }
    public String getMembershipName() { return membershipName; }
    public void setMembershipName(String membershipName) { this.membershipName = membershipName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
}
