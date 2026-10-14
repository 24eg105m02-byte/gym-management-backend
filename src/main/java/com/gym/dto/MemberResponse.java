package com.gym.dto;

import com.gym.entity.Member;
import com.gym.enums.EntityStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MemberResponse {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String emergencyContact;
    private Long membershipId;
    private String membershipName;
    private Long trainerId;
    private String trainerName;
    private LocalDate membershipStartDate;
    private LocalDate membershipEndDate;
    private EntityStatus status;
    private LocalDateTime createdAt;

    public static MemberResponse from(Member m) {
        MemberResponse r = new MemberResponse();
        r.id = m.getId();
        if (m.getUser() != null) {
            r.userId = m.getUser().getId();
            r.name = m.getUser().getName();
            r.email = m.getUser().getEmail();
            r.phone = m.getUser().getPhone();
        }
        r.dateOfBirth = m.getDateOfBirth();
        r.gender = m.getGender();
        r.address = m.getAddress();
        r.emergencyContact = m.getEmergencyContact();
        if (m.getMembership() != null) {
            r.membershipId = m.getMembership().getId();
            r.membershipName = m.getMembership().getName();
        }
        if (m.getTrainer() != null) {
            r.trainerId = m.getTrainer().getId();
            if (m.getTrainer().getUser() != null) {
                r.trainerName = m.getTrainer().getUser().getName();
            }
        }
        r.membershipStartDate = m.getMembershipStartDate();
        r.membershipEndDate = m.getMembershipEndDate();
        r.status = m.getStatus();
        r.createdAt = m.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
    public Long getMembershipId() { return membershipId; }
    public void setMembershipId(Long membershipId) { this.membershipId = membershipId; }
    public String getMembershipName() { return membershipName; }
    public void setMembershipName(String membershipName) { this.membershipName = membershipName; }
    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }
    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }
    public LocalDate getMembershipStartDate() { return membershipStartDate; }
    public void setMembershipStartDate(LocalDate membershipStartDate) { this.membershipStartDate = membershipStartDate; }
    public LocalDate getMembershipEndDate() { return membershipEndDate; }
    public void setMembershipEndDate(LocalDate membershipEndDate) { this.membershipEndDate = membershipEndDate; }
    public EntityStatus getStatus() { return status; }
    public void setStatus(EntityStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
