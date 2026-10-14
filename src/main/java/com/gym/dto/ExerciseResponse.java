package com.gym.dto;

import com.gym.entity.Exercise;

public class ExerciseResponse {
    private Long id;
    private String name;
    private Integer sets;
    private Integer reps;
    private Double weight;
    private String restTime;
    private String notes;

    public static ExerciseResponse from(Exercise e) {
        ExerciseResponse r = new ExerciseResponse();
        r.id = e.getId();
        r.name = e.getName();
        r.sets = e.getSets();
        r.reps = e.getReps();
        r.weight = e.getWeight();
        r.restTime = e.getRestTime();
        r.notes = e.getNotes();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
