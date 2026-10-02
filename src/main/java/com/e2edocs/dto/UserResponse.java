package com.e2edocs.dto;

import com.e2edocs.entity.enums.UserRole;
import com.e2edocs.entity.enums.UserStatus;
import java.time.Instant;

public class UserResponse {
    private String id;
    private String name;
    private String email;
    private UserRole role;
    private String department;
    private String departmentId;
    private UserStatus status;
    private String avatar;
    private Instant lastActive;
    private Instant createdAt;

    public UserResponse() {
    }

    public UserResponse(String id, String name, String email, UserRole role, String department, String departmentId, UserStatus status, String avatar, Instant lastActive, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.department = department;
        this.departmentId = departmentId;
        this.status = status;
        this.avatar = avatar;
        this.lastActive = lastActive;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Instant getLastActive() {
        return lastActive;
    }

    public void setLastActive(Instant lastActive) {
        this.lastActive = lastActive;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
