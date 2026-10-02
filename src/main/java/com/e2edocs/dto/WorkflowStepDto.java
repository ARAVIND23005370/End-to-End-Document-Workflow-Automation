package com.e2edocs.dto;

public class WorkflowStepDto {
    private String id;
    private String name;
    private String type;
    private String status = "completed";
    private Integer order = 1;

    public WorkflowStepDto() {
    }

    public WorkflowStepDto(String id, String name, String type, String status, Integer order) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status != null ? status : "completed";
        this.order = order != null ? order : 1;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
