package com.gym.dto;

import com.gym.entity.Feedback;
import java.time.LocalDateTime;

public class FeedbackResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long trainerId;
    private String trainerName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public static FeedbackResponse from(Feedback f) {
        FeedbackResponse r = new FeedbackResponse();
        r.id = f.getId();
        if (f.getMember() != null) {
            r.memberId = f.getMember().getId();
            if (f.getMember().getUser() != null) {
                r.memberName = f.getMember().getUser().getName();
            }
        }
        if (f.getTrainer() != null) {
            r.trainerId = f.getTrainer().getId();
            if (f.getTrainer().getUser() != null) {
                r.trainerName = f.getTrainer().getUser().getName();
            }
        }
        r.rating = f.getRating();
        r.comment = f.getComment();
        r.createdAt = f.getCreatedAt();
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
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
