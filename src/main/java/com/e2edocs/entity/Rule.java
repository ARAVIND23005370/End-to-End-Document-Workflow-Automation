package com.e2edocs.entity;

import com.e2edocs.entity.enums.RuleStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rules")
public class Rule {

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
    private RuleStatus status = RuleStatus.ACTIVE;

    @Column(name = "evaluation_order", nullable = false)
    private Integer evaluationOrder = 1;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "match_count")
    private Integer matchCount = 0;

    @Column(name = "last_triggered")
    private Instant lastTriggered;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sortOrder ASC")
    private List<RuleConditionGroup> conditionGroups = new ArrayList<>();

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sortOrder ASC")
    private List<RuleAction> actions = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public Rule() {
    }

    public Rule(String id, String organizationId, String name, String description, RuleStatus status, Integer evaluationOrder) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.description = description;
        this.status = status;
        this.evaluationOrder = evaluationOrder != null ? evaluationOrder : 1;
        this.matchCount = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        this.updatedAt = Instant.now();
        if (this.matchCount == null) {
            this.matchCount = 0;
        }
        if (this.evaluationOrder == null) {
            this.evaluationOrder = 1;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addConditionGroup(RuleConditionGroup group) {
        group.setRule(this);
        this.conditionGroups.add(group);
    }

    public void addAction(RuleAction action) {
        action.setRule(this);
        this.actions.add(action);
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

    public RuleStatus getStatus() {
        return status;
    }

    public void setStatus(RuleStatus status) {
        this.status = status;
    }

    public Integer getEvaluationOrder() {
        return evaluationOrder;
    }

    public void setEvaluationOrder(Integer evaluationOrder) {
        this.evaluationOrder = evaluationOrder;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getMatchCount() {
        return matchCount;
    }

    public void setMatchCount(Integer matchCount) {
        this.matchCount = matchCount;
    }

    public Instant getLastTriggered() {
        return lastTriggered;
    }

    public void setLastTriggered(Instant lastTriggered) {
        this.lastTriggered = lastTriggered;
    }

    public List<RuleConditionGroup> getConditionGroups() {
        return conditionGroups;
    }

    public void setConditionGroups(List<RuleConditionGroup> conditionGroups) {
        this.conditionGroups = conditionGroups;
    }

    public List<RuleAction> getActions() {
        return actions;
    }

    public void setActions(List<RuleAction> actions) {
        this.actions = actions;
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
