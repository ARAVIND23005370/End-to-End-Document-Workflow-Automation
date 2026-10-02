package com.e2edocs.entity;

import com.e2edocs.entity.enums.WorkflowStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workflows")
public class Workflow {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "organization_id", length = 64, nullable = false)
    private String organizationId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private WorkflowStatus status = WorkflowStatus.ACTIVE;

    @Column(name = "trigger_name")
    private String trigger = "On document arrival";

    @Column(name = "owner")
    private String owner;

    @Column(name = "documents_processed")
    private Integer documentsProcessed = 0;

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("stepOrder ASC")
    private List<WorkflowStep> steps = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public Workflow() {
    }

    public Workflow(String id, String organizationId, String name, String description, WorkflowStatus status, String owner) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.description = description;
        this.status = status != null ? status : WorkflowStatus.ACTIVE;
        this.owner = owner;
        this.documentsProcessed = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        this.updatedAt = Instant.now();
        if (this.documentsProcessed == null) {
            this.documentsProcessed = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addStep(WorkflowStep step) {
        step.setWorkflow(this);
        this.steps.add(step);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
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

    public Integer getDocumentsProcessed() {
        return documentsProcessed;
    }

    public void setDocumentsProcessed(Integer documentsProcessed) {
        this.documentsProcessed = documentsProcessed;
    }

    public List<WorkflowStep> getSteps() {
        return steps;
    }

    public void setSteps(List<WorkflowStep> steps) {
        this.steps = steps;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
