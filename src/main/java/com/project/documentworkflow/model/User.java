package com.project.documentworkflow.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    // ADMIN, STAFF, VIEWER
    private String role;

    // User permission: do they allow auto-email on reject?
    private Boolean emailNotifyOnReject = false;

    // Getters and Setters
    public Long getUserId() { return userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getEmailNotifyOnReject() { return emailNotifyOnReject; }
    public void setEmailNotifyOnReject(Boolean emailNotifyOnReject) {
        this.emailNotifyOnReject = emailNotifyOnReject;
    }
}
