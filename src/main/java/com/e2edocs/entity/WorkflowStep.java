package com.e2edocs.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "workflow_steps")
public class WorkflowStep {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    @JsonIgnore
    private Workflow workflow;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", length = 64)
    private String type;

    @Column(name = "status", length = 32)
    private String status = "completed";

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder = 1;

    public WorkflowStep() {
    }

    public WorkflowStep(String id, Workflow workflow, String name, String type, String status, Integer stepOrder) {
        this.id = id;
        this.workflow = workflow;
        this.name = name;
        this.type = type;
        this.status = status != null ? status : "completed";
        this.stepOrder = stepOrder != null ? stepOrder : 1;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
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

    public Integer getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }
}
