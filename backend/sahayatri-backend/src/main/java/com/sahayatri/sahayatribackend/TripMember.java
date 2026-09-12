package com.sahayatri.sahayatribackend;

import jakarta.persistence.*;

@Entity
@Table(name = "trip_members")
public class TripMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long groupId;

    private String name;

    private String email;

    private String phone;

    private String upiId;

    public TripMember() {
    }

    public TripMember(Long groupId, String name, String email,
                      String phone, String upiId) {
        this.groupId = groupId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.upiId = upiId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }
}