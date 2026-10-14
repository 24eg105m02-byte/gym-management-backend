package com.gym.dto;

import jakarta.validation.constraints.NotBlank;

public class ExerciseRequest {

    @NotBlank(message = "Exercise name is required")
    private String name;

    private Integer sets;
    private Integer reps;
    private Double weight;
    private String restTime;
    private String notes;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSets() { return sets; }
    public void setSets(Integer sets) { this.sets = sets; }
    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public String getRestTime() { return restTime; }
    public void setRestTime(String restTime) { this.restTime = restTime; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
