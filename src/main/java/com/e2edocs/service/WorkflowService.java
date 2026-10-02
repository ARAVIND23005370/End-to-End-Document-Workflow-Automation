package com.e2edocs.service;

import com.e2edocs.dto.WorkflowInputDto;
import com.e2edocs.dto.WorkflowResponse;
import com.e2edocs.dto.WorkflowStepDto;
import com.e2edocs.entity.Workflow;
import com.e2edocs.entity.WorkflowStep;
import com.e2edocs.entity.enums.AuditAction;
import com.e2edocs.entity.enums.AuditStatus;
import com.e2edocs.entity.enums.WorkflowStatus;
import com.e2edocs.exception.ResourceNotFoundException;
import com.e2edocs.repository.WorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final AuditService auditService;

    public WorkflowService(WorkflowRepository workflowRepository, AuditService auditService) {
        this.workflowRepository = workflowRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<WorkflowResponse> getAllWorkflows(String organizationId) {
        return workflowRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkflowResponse getWorkflowById(String id) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found: " + id));
        return mapToResponse(workflow);
    }

    @Transactional
    public WorkflowResponse createWorkflow(String organizationId, WorkflowInputDto input, String owner) {
        String workflowId = "wf-" + UUID.randomUUID().toString().substring(0, 8);
        Workflow workflow = new Workflow(
                workflowId,
                organizationId,
                input.getName(),
                input.getDescription(),
                input.getStatus() != null ? input.getStatus() : WorkflowStatus.ACTIVE,
                owner
        );
        if (input.getTrigger() != null) {
            workflow.setTrigger(input.getTrigger());
        }

        populateSteps(workflow, input);

        Workflow saved = workflowRepository.save(workflow);

        auditService.log(organizationId, "SYSTEM", owner, AuditAction.CREATED,
                "Workflow", saved.getId(), AuditStatus.SUCCESS, "Created workflow: " + saved.getName(), "SYSTEM");

        return mapToResponse(saved);
    }

    @Transactional
    public WorkflowResponse updateWorkflow(String id, WorkflowInputDto input, String updatedBy) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found: " + id));

        workflow.setName(input.getName());
        workflow.setDescription(input.getDescription());
        if (input.getStatus() != null) {
            workflow.setStatus(input.getStatus());
        }
        if (input.getTrigger() != null) {
            workflow.setTrigger(input.getTrigger());
        }

        workflow.getSteps().clear();
        populateSteps(workflow, input);

        Workflow saved = workflowRepository.save(workflow);

        auditService.log(saved.getOrganizationId(), "SYSTEM", updatedBy, AuditAction.UPDATED,
                "Workflow", saved.getId(), AuditStatus.SUCCESS, "Updated workflow: " + saved.getName(), "SYSTEM");

        return mapToResponse(saved);
    }

    @Transactional
    public WorkflowResponse updateStepStatus(String workflowId, String stepId, String status, String updatedBy) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found: " + workflowId));

        WorkflowStep step = workflow.getSteps().stream()
                .filter(s -> s.getId().equals(stepId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowStep not found: " + stepId));

        step.setStatus(status != null ? status : "completed");
        Workflow saved = workflowRepository.save(workflow);

        auditService.log(saved.getOrganizationId(), "SYSTEM", updatedBy, AuditAction.UPDATED,
                "WorkflowStep", step.getId(), AuditStatus.SUCCESS,
                "Updated workflow step '" + step.getName() + "' status to: " + status, "SYSTEM");

        return mapToResponse(saved);
    }

    private void populateSteps(Workflow workflow, WorkflowInputDto input) {
        if (input.getSteps() != null) {
            int idx = 1;
            for (WorkflowStepDto sDto : input.getSteps()) {
                String sId = sDto.getId() != null ? sDto.getId() : "ws-" + UUID.randomUUID().toString().substring(0, 8);
                WorkflowStep step = new WorkflowStep(
                        sId,
                        workflow,
                        sDto.getName(),
                        sDto.getType(),
                        sDto.getStatus(),
                        sDto.getOrder() != null ? sDto.getOrder() : idx++
                );
                workflow.addStep(step);
            }
        }
    }

    public WorkflowResponse mapToResponse(Workflow wf) {
        WorkflowResponse res = new WorkflowResponse();
        res.setId(wf.getId());
        res.setName(wf.getName());
        res.setDescription(wf.getDescription());
        res.setStatus(wf.getStatus());
        res.setTrigger(wf.getTrigger());
        res.setOwner(wf.getOwner());
        res.setDocumentsProcessed(wf.getDocumentsProcessed());
        res.setCreatedAt(wf.getCreatedAt());
        res.setUpdatedAt(wf.getUpdatedAt());

        if (wf.getSteps() != null) {
            res.setSteps(wf.getSteps().stream()
                    .map(s -> new WorkflowStepDto(s.getId(), s.getName(), s.getType(), s.getStatus(), s.getStepOrder()))
                    .collect(Collectors.toList()));
        }

        return res;
    }
}
