package com.e2edocs.dto;

import com.e2edocs.entity.enums.WorkflowStatus;
import java.util.ArrayList;
import java.util.List;

public class WorkflowInputDto {
    private String name;
    private String description;
    private WorkflowStatus status = WorkflowStatus.ACTIVE;
    private List<WorkflowStepDto> steps = new ArrayList<>();
    private String trigger = "On document arrival";
    private String owner;

    public WorkflowInputDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public WorkflowStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowStatus status) {
        this.status = status;
    }

    public List<WorkflowStepDto> getSteps() {
        return steps;
    }

    public void setSteps(List<WorkflowStepDto> steps) {
        this.steps = steps;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
