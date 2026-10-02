package com.e2edocs.repository;

import com.e2edocs.entity.Workflow;
import com.e2edocs.entity.enums.WorkflowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, String> {
    List<Workflow> findByOrganizationId(String organizationId);
    List<Workflow> findByOrganizationIdAndStatus(String organizationId, WorkflowStatus status);
    long countByOrganizationIdAndStatus(String organizationId, WorkflowStatus status);
}
