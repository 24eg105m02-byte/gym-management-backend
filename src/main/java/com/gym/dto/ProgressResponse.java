package com.gym.dto;

import com.gym.entity.Progress;
import java.time.LocalDate;

public class ProgressResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private Double weight;
    private Double height;
    private Double bmi;
    private Double bodyFat;
    private Double strength;
    private String notes;
    private LocalDate recordDate;

    public static ProgressResponse from(Progress p) {
        ProgressResponse r = new ProgressResponse();
        r.id = p.getId();
        if (p.getMember() != null) {
            r.memberId = p.getMember().getId();
            if (p.getMember().getUser() != null) {
                r.memberName = p.getMember().getUser().getName();
            }
        }
        r.weight = p.getWeight();
        r.height = p.getHeight();
        r.bmi = p.getBmi();
        r.bodyFat = p.getBodyFat();
        r.strength = p.getStrength();
        r.notes = p.getNotes();
        r.recordDate = p.getRecordDate();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }
    public Double getBmi() { return bmi; }
    public void setBmi(Double bmi) { this.bmi = bmi; }
    public Double getBodyFat() { return bodyFat; }
    public void setBodyFat(Double bodyFat) { this.bodyFat = bodyFat; }
    public Double getStrength() { return strength; }
    public void setStrength(Double strength) { this.strength = strength; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
}
