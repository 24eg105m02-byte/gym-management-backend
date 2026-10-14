package com.gym.dto;

import com.gym.entity.Trainer;
import com.gym.enums.EntityStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TrainerResponse {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String specialization;
    private Integer experience;
    private BigDecimal salary;
    private LocalDate joiningDate;
    private EntityStatus status;
    private LocalDateTime createdAt;
    private long assignedMemberCount;

    public static TrainerResponse from(Trainer t, long memberCount) {
        TrainerResponse r = new TrainerResponse();
        r.id = t.getId();
        if (t.getUser() != null) {
            r.userId = t.getUser().getId();
            r.name = t.getUser().getName();
            r.email = t.getUser().getEmail();
            r.phone = t.getUser().getPhone();
        }
        r.specialization = t.getSpecialization();
        r.experience = t.getExperience();
        r.salary = t.getSalary();
        r.joiningDate = t.getJoiningDate();
        r.status = t.getStatus();
        r.createdAt = t.getCreatedAt();
        r.assignedMemberCount = memberCount;
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
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public Integer getExperience() { return experience; }
    public void setExperience(Integer experience) { this.experience = experience; }
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }
    public EntityStatus getStatus() { return status; }
    public void setStatus(EntityStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public long getAssignedMemberCount() { return assignedMemberCount; }
    public void setAssignedMemberCount(long assignedMemberCount) { this.assignedMemberCount = assignedMemberCount; }
}
