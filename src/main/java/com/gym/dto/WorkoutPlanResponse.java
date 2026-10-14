package com.gym.dto;

import com.gym.entity.WorkoutPlan;
import com.gym.enums.EntityStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class WorkoutPlanResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long trainerId;
    private String trainerName;
    private String title;
    private String goal;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private EntityStatus status;
    private LocalDateTime createdAt;
    private List<ExerciseResponse> exercises;

    public static WorkoutPlanResponse from(WorkoutPlan w) {
        WorkoutPlanResponse r = new WorkoutPlanResponse();
        r.id = w.getId();
        if (w.getMember() != null) {
            r.memberId = w.getMember().getId();
            if (w.getMember().getUser() != null) {
                r.memberName = w.getMember().getUser().getName();
            }
        }
        if (w.getTrainer() != null) {
            r.trainerId = w.getTrainer().getId();
            if (w.getTrainer().getUser() != null) {
                r.trainerName = w.getTrainer().getUser().getName();
            }
        }
        r.title = w.getTitle();
        r.goal = w.getGoal();
        r.description = w.getDescription();
        r.startDate = w.getStartDate();
        r.endDate = w.getEndDate();
        r.status = w.getStatus();
        r.createdAt = w.getCreatedAt();
        if (w.getExercises() != null) {
            r.exercises = w.getExercises().stream().map(ExerciseResponse::from).collect(Collectors.toList());
        }
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }
    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public EntityStatus getStatus() { return status; }
    public void setStatus(EntityStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<ExerciseResponse> getExercises() { return exercises; }
    public void setExercises(List<ExerciseResponse> exercises) { this.exercises = exercises; }
}
