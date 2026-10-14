package com.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WorkoutPlanRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotBlank(message = "Title is required")
    private String title;

    private String goal;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private List<ExerciseRequest> exercises = new ArrayList<>();

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<ExerciseRequest> getExercises() { return exercises; }
    public void setExercises(List<ExerciseRequest> exercises) { this.exercises = exercises; }
}
