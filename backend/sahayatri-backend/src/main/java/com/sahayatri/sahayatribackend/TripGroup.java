package com.sahayatri.sahayatribackend;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_groups")
public class TripGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tripId;

    private String groupName;

    private String createdByEmail;

    private LocalDateTime createdAt;

    public TripGroup() {
    }

    public TripGroup(Long tripId, String groupName, String createdByEmail) {
        this.tripId = tripId;
        this.groupName = groupName;
        this.createdByEmail = createdByEmail;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}