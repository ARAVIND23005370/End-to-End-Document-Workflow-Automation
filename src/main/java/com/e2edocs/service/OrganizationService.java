package com.e2edocs.service;

import com.e2edocs.dto.*;
import com.e2edocs.entity.Department;
import com.e2edocs.entity.DocumentCategory;
import com.e2edocs.entity.Organization;
import com.e2edocs.entity.Team;
import com.e2edocs.entity.enums.AuditAction;
import com.e2edocs.entity.enums.AuditStatus;
import com.e2edocs.exception.ResourceNotFoundException;
import com.e2edocs.repository.DepartmentRepository;
import com.e2edocs.repository.DocumentCategoryRepository;
import com.e2edocs.repository.OrganizationRepository;
import com.e2edocs.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final DocumentCategoryRepository documentCategoryRepository;
    private final AuditService auditService;

    public OrganizationService(
            OrganizationRepository organizationRepository,
            DepartmentRepository departmentRepository,
            TeamRepository teamRepository,
            DocumentCategoryRepository documentCategoryRepository,
            AuditService auditService) {
        this.organizationRepository = organizationRepository;
        this.departmentRepository = departmentRepository;
        this.teamRepository = teamRepository;
        this.documentCategoryRepository = documentCategoryRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getDepartments(String organizationId) {
        return departmentRepository.findByOrganizationId(organizationId).stream()
                .map(d -> new DepartmentResponse(d.getId(), d.getName(), d.getCode(), d.getDescription()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getTeams(String organizationId) {
        return teamRepository.findByOrganizationId(organizationId).stream()
                .map(t -> new TeamResponse(t.getId(), t.getName(), t.getDepartmentId(), t.getLeaderId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentCategoryResponse> getCategories(String organizationId) {
        return documentCategoryRepository.findByOrganizationId(organizationId).stream()
                .map(c -> new DocumentCategoryResponse(c.getId(), c.getName(), c.getDescription()))
                .collect(Collectors.toList());
    }

    @Transactional
    public OrganizationResponse updateSettings(String organizationId, OrganizationSettingsRequest request, String userId, String userName) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + organizationId));

        if (request.getName() != null && !request.getName().isBlank()) {
            org.setName(request.getName());
        }
        if (request.getDomain() != null) {
            org.setDomain(request.getDomain());
        }
        if (request.getDescription() != null) {
            org.setDescription(request.getDescription());
        }

        Organization saved = organizationRepository.save(org);

        auditService.log(organizationId, userId, userName, AuditAction.UPDATED,
                "Organization", saved.getId(), AuditStatus.SUCCESS, "Updated organization settings", "SYSTEM");

        return new OrganizationResponse(saved.getId(), saved.getName(), saved.getCode(), saved.getDescription(), saved.getDomain(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getOrganization(String organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + organizationId));
        return new OrganizationResponse(org.getId(), org.getName(), org.getCode(), org.getDescription(), org.getDomain(), org.getCreatedAt());
    }
}
