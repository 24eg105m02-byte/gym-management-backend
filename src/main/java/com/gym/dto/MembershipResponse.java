package com.gym.dto;

import com.gym.entity.Membership;
import com.gym.enums.EntityStatus;
import java.math.BigDecimal;

public class MembershipResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer duration;
    private String features;
    private EntityStatus status;

    public static MembershipResponse from(Membership m) {
        MembershipResponse r = new MembershipResponse();
        r.id = m.getId();
        r.name = m.getName();
        r.description = m.getDescription();
        r.price = m.getPrice();
        r.duration = m.getDuration();
        r.features = m.getFeatures();
        r.status = m.getStatus();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }
    public EntityStatus getStatus() { return status; }
    public void setStatus(EntityStatus status) { this.status = status; }
}
