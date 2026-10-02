package com.e2edocs.dto;

public class TeamResponse {
    private String id;
    private String name;
    private String departmentId;
    private String leaderId;

    public TeamResponse() {
    }

    public TeamResponse(String id, String name, String departmentId, String leaderId) {
        this.id = id;
        this.name = name;
        this.departmentId = departmentId;
        this.leaderId = leaderId;
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

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }
}
