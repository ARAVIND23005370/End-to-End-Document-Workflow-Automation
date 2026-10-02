package com.e2edocs.controller;

import com.e2edocs.dto.*;
import com.e2edocs.security.UserPrincipal;
import com.e2edocs.service.OrganizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organization")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentResponse>> getDepartments(@AuthenticationPrincipal UserPrincipal principal) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        List<DepartmentResponse> departments = organizationService.getDepartments(orgId);
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/teams")
    public ResponseEntity<List<TeamResponse>> getTeams(@AuthenticationPrincipal UserPrincipal principal) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        List<TeamResponse> teams = organizationService.getTeams(orgId);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<DocumentCategoryResponse>> getCategories(@AuthenticationPrincipal UserPrincipal principal) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        List<DocumentCategoryResponse> categories = organizationService.getCategories(orgId);
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/settings")
    public ResponseEntity<OrganizationResponse> updateSettings(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody OrganizationSettingsRequest request) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        String userId = principal != null ? principal.getId() : "SYSTEM";
        String userName = principal != null ? principal.getName() : "System User";
        OrganizationResponse response = organizationService.updateSettings(
                orgId, request, userId, userName);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<OrganizationResponse> getOrganization(@AuthenticationPrincipal UserPrincipal principal) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        OrganizationResponse response = organizationService.getOrganization(orgId);
        return ResponseEntity.ok(response);
    }
}
