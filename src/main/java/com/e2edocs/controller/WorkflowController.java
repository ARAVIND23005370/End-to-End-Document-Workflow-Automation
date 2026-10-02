package com.e2edocs.controller;

import com.e2edocs.dto.WorkflowInputDto;
import com.e2edocs.dto.WorkflowResponse;
import com.e2edocs.security.UserPrincipal;
import com.e2edocs.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    public ResponseEntity<List<WorkflowResponse>> getAllWorkflows(@AuthenticationPrincipal UserPrincipal principal) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        List<WorkflowResponse> workflows = workflowService.getAllWorkflows(orgId);
        return ResponseEntity.ok(workflows);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkflowResponse> getWorkflowById(@PathVariable String id) {
        WorkflowResponse workflow = workflowService.getWorkflowById(id);
        return ResponseEntity.ok(workflow);
    }

    @PostMapping
    public ResponseEntity<WorkflowResponse> createWorkflow(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody WorkflowInputDto input) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        String userName = principal != null ? principal.getName() : "System User";
        WorkflowResponse workflow = workflowService.createWorkflow(orgId, input, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(workflow);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkflowResponse> updateWorkflow(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody WorkflowInputDto input) {
        String userName = principal != null ? principal.getName() : "System User";
        WorkflowResponse workflow = workflowService.updateWorkflow(id, input, userName);
        return ResponseEntity.ok(workflow);
    }
}
