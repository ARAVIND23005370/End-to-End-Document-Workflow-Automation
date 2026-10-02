package com.e2edocs.dto;

import java.time.Instant;

public class OrganizationResponse {
    private String id;
    private String name;
    private String code;
    private String description;
    private String domain;
    private Instant createdAt;

    public OrganizationResponse() {
    }

    public OrganizationResponse(String id, String name, String code, String description, String domain, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.domain = domain;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
