package com.sahayatri.sahayatribackend;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_expenses")
public class TripExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long groupId;

    private String description;

    private double amount;

    private Long paidByMemberId;

    private LocalDateTime createdAt;

    public TripExpense() {
    }

    public TripExpense(Long groupId, String description,
                       double amount, Long paidByMemberId) {
        this.groupId = groupId;
        this.description = description;
        this.amount = amount;
        this.paidByMemberId = paidByMemberId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Long getPaidByMemberId() {
        return paidByMemberId;
    }

    public void setPaidByMemberId(Long paidByMemberId) {
        this.paidByMemberId = paidByMemberId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}