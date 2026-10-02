package com.e2edocs.dto;

public class OrganizationSettingsRequest {
    private String name;
    private String domain;
    private String description;

    public OrganizationSettingsRequest() {
    }

    public OrganizationSettingsRequest(String name, String domain, String description) {
        this.name = name;
        this.domain = domain;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
