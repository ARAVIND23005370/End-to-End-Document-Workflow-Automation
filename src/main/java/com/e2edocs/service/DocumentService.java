package com.e2edocs.service;

import com.e2edocs.dto.*;
import com.e2edocs.entity.Document;
import com.e2edocs.entity.enums.*;
import com.e2edocs.exception.BadRequestException;
import com.e2edocs.exception.ResourceNotFoundException;
import com.e2edocs.repository.DocumentRepository;
import com.e2edocs.repository.UserRepository;
import com.e2edocs.service.email.EmailService;
import com.e2edocs.service.extraction.DocumentExtractionResult;
import com.e2edocs.service.extraction.DocumentExtractionService;
import com.e2edocs.service.storage.StorageService;
import com.e2edocs.service.storage.StoredFileMetadata;
import com.e2edocs.service.validation.FileValidatorService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final FileValidatorService fileValidatorService;
    private final DocumentExtractionService documentExtractionService;
    private final RuleEngineService ruleEngineService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    public DocumentService(
            DocumentRepository documentRepository,
            UserRepository userRepository,
            StorageService storageService,
            FileValidatorService fileValidatorService,
            DocumentExtractionService documentExtractionService,
            RuleEngineService ruleEngineService,
            AuditService auditService,
            NotificationService notificationService,
            EmailService emailService) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.fileValidatorService = fileValidatorService;
        this.documentExtractionService = documentExtractionService;
        this.ruleEngineService = ruleEngineService;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional(readOnly = true)
    public PageResponse<DocumentResponse> getDocuments(
            String organizationId,
            String search,
            String statusStr,
            String priorityStr,
            String sourceStr,
            String department,
            int page,
            int pageSize,
            String sortBy,
            String sortOrder) {

        int pageNumber = Math.max(0, page - 1);
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));

        Specification<Document> spec = buildDocumentSpecification(organizationId, search, statusStr, priorityStr, sourceStr, department);

        Page<Document> resultPage = documentRepository.findAll(spec, pageable);
        List<DocumentResponse> dtos = resultPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(dtos, resultPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public byte[] exportDocumentsCsv(
            String organizationId,
            String search,
            String statusStr,
            String priorityStr,
            String sourceStr,
            String department,
            String sortBy,
            String sortOrder) {

        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField);

        Specification<Document> spec = buildDocumentSpecification(organizationId, search, statusStr, priorityStr, sourceStr, department);
        List<Document> documents = documentRepository.findAll(spec, sort);

        StringBuilder csv = new StringBuilder();
        csv.append("Document ID,Filename,Type,Source,Status,Priority,Department,Assigned User,Sender Email,Sender Name,Created At,Updated At\n");

        for (Document doc : documents) {
            csv.append(escapeCsv(doc.getId())).append(",")
               .append(escapeCsv(doc.getName())).append(",")
               .append(escapeCsv(doc.getType())).append(",")
               .append(escapeCsv(doc.getSource() != null ? doc.getSource().getValue() : "")).append(",")
               .append(escapeCsv(doc.getStatus() != null ? doc.getStatus().getValue() : "")).append(",")
               .append(escapeCsv(doc.getPriority() != null ? doc.getPriority().getValue() : "")).append(",")
               .append(escapeCsv(doc.getDepartment() != null ? doc.getDepartment() : "")).append(",")
               .append(escapeCsv(doc.getAssignedTo() != null ? doc.getAssignedTo() : "")).append(",")
               .append(escapeCsv(doc.getOriginalSenderEmail() != null ? doc.getOriginalSenderEmail() : "")).append(",")
               .append(escapeCsv(doc.getOriginalSenderName() != null ? doc.getOriginalSenderName() : "")).append(",")
               .append(escapeCsv(doc.getCreatedAt() != null ? doc.getCreatedAt().toString() : "")).append(",")
               .append(escapeCsv(doc.getUpdatedAt() != null ? doc.getUpdatedAt().toString() : "")).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String val) {
        if (val == null) return "\"\"";
        String escaped = val.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private Specification<Document> buildDocumentSpecification(
            String organizationId,
            String search,
            String statusStr,
            String priorityStr,
            String sourceStr,
            String department) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("organizationId"), organizationId));

            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), searchPattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), searchPattern);
                Predicate senderEmailMatch = cb.like(cb.lower(root.get("originalSenderEmail")), searchPattern);
                Predicate senderNameMatch = cb.like(cb.lower(root.get("originalSenderName")), searchPattern);
                predicates.add(cb.or(nameMatch, descMatch, senderEmailMatch, senderNameMatch));
            }

            if (statusStr != null && !statusStr.isBlank()) {
                try {
                    DocumentStatus status = DocumentStatus.fromValue(statusStr);
                    predicates.add(cb.equal(root.get("status"), status));
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (priorityStr != null && !priorityStr.isBlank()) {
                try {
                    Priority priority = Priority.fromValue(priorityStr);
                    predicates.add(cb.equal(root.get("priority"), priority));
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (sourceStr != null && !sourceStr.isBlank()) {
                try {
                    DocumentSource source = DocumentSource.fromValue(sourceStr);
                    predicates.add(cb.equal(root.get("source"), source));
                } catch (IllegalArgumentException ignored) {
                }
            }

            if (department != null && !department.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("department")), department.trim().toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true)
    public DocumentDetailResponse getDocumentById(String id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

        DocumentDetailResponse detail = new DocumentDetailResponse();
        copyToResponse(doc, detail);

        detail.setContentPreview(doc.getContentPreview() != null ? doc.getContentPreview() : "Document preview content...");

        // Detected Info
        List<DetectedInfoItem> infoList = new ArrayList<>();
        if (doc.getType() != null) {
            infoList.add(new DetectedInfoItem("Document Type", doc.getType(), 0.98));
        }
        if (doc.getOriginalSenderEmail() != null) {
            infoList.add(new DetectedInfoItem("Original Sender Email", doc.getOriginalSenderEmail()));
        }
        if (doc.getOriginalSenderName() != null) {
            infoList.add(new DetectedInfoItem("Original Sender Name", doc.getOriginalSenderName()));
        }
        if (doc.getSource() != null) {
            infoList.add(new DetectedInfoItem("Source Channel", doc.getSource().getValue()));
        }
        detail.setDetectedInfo(infoList);

        // Audit History
        detail.setAuditHistory(auditService.getAuditHistoryForResource(doc.getId()));

        return detail;
    }

    @Transactional
    public DocumentResponse createDocument(String organizationId, MultipartFile file, DocumentCreateRequest request, String userId, String userName) {
        String docId = "DOC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String name = (request != null && request.getName() != null && !request.getName().isBlank())
                ? request.getName()
                : (file != null && file.getOriginalFilename() != null ? file.getOriginalFilename() : "Untitled_Document.pdf");

        Document doc = new Document(docId, organizationId, name, request != null && request.getType() != null ? request.getType() : "General Document");

        if (request != null) {
            if (request.getDescription() != null) doc.setDescription(request.getDescription());
            if (request.getPriority() != null) doc.setPriority(request.getPriority());
            if (request.getStatus() != null) doc.setStatus(request.getStatus());
            if (request.getSource() != null) doc.setSource(request.getSource());
            if (request.getOriginalSenderEmail() != null) doc.setOriginalSenderEmail(request.getOriginalSenderEmail());
            if (request.getOriginalSenderName() != null) doc.setOriginalSenderName(request.getOriginalSenderName());
            if (request.getDepartment() != null) doc.setDepartment(request.getDepartment());
            if (request.getDepartmentId() != null) doc.setDepartmentId(request.getDepartmentId());
            if (request.getAssignedTo() != null) doc.setAssignedTo(request.getAssignedTo());
            if (request.getAssignedToId() != null) doc.setAssignedToId(request.getAssignedToId());
            if (request.getTags() != null) doc.setTags(request.getTags());

            if (request.getMetadata() != null) {
                try {
                    doc.setMetadataJson(objectMapper.writeValueAsString(request.getMetadata()));
                } catch (Exception ignored) {
                }
            }
        }

        if (file != null && !file.isEmpty()) {
            // 1. Validate uploaded file
            fileValidatorService.validate(file);

            // 2. Store file securely
            StoredFileMetadata stored = storageService.storeWithMetadata(file);
            doc.setStoragePath(stored.getStorageKey());
            doc.setFileName(stored.getOriginalFilename());
            doc.setSize(stored.getSize());
            doc.setFileExtension(stored.getFileExtension());

            // 3. Extract text and content preview
            try {
                DocumentExtractionResult extraction = documentExtractionService.extract(
                        file.getInputStream(),
                        stored.getOriginalFilename(),
                        stored.getContentType()
                );

                doc.setExtractedText(extraction.getExtractedText());
                doc.setContentPreview(extraction.getContentPreview());

                if (doc.getDescription() == null || doc.getDescription().isBlank()) {
                    doc.setDescription(extraction.getContentPreview());
                }

                // Merge extraction metadata if any
                if (!extraction.getMetadata().isEmpty()) {
                    Map<String, Object> currentMeta = new HashMap<>();
                    if (doc.getMetadataJson() != null && !doc.getMetadataJson().isBlank()) {
                        try {
                            currentMeta = objectMapper.readValue(doc.getMetadataJson(), new TypeReference<Map<String, Object>>() {});
                        } catch (Exception ignored) {
                        }
                    }
                    currentMeta.putAll(extraction.getMetadata());
                    currentMeta.put("extractor", extraction.getExtractorUsed());
                    currentMeta.put("ocr_used", extraction.isOcrUsed());
                    doc.setMetadataJson(objectMapper.writeValueAsString(currentMeta));
                }
            } catch (Exception e) {
                doc.setContentPreview("Content preview pending extraction: " + e.getMessage());
            }
        }

        Document saved = documentRepository.save(doc);

        auditService.log(organizationId, userId, userName, AuditAction.UPLOADED,
                "Document", saved.getId(), AuditStatus.SUCCESS, "Uploaded document: " + saved.getName(), "SYSTEM");

        // 4. Evaluate Rule Engine on the ingested document
        try {
            ruleEngineService.evaluateDocument(saved);
        } catch (Exception e) {
            // Log but don't fail upload
            auditService.log(organizationId, "SYSTEM", "Rule Engine", AuditAction.ROUTED,
                    "Document", saved.getId(), AuditStatus.FAILURE, "Rule evaluation error: " + e.getMessage(), "SYSTEM");
        }

        return mapToResponse(saved);
    }

    @Transactional
    public void sendDocumentByEmail(String documentId, DocumentSendEmailRequest request, String userId, String userName, String callerOrgId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        if (callerOrgId != null && !callerOrgId.equals(doc.getOrganizationId())) {
            throw new ResourceNotFoundException("Document not found: " + documentId);
        }

        if (request == null || request.getRecipient() == null || request.getRecipient().isBlank()) {
            throw new BadRequestException("Recipient email address is required.");
        }
        if (!request.getRecipient().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BadRequestException("Invalid recipient email address format: " + request.getRecipient());
        }
        if (request.getSubject() == null || request.getSubject().isBlank()) {
            throw new BadRequestException("Email subject is required.");
        }

        byte[] attachmentBytes;
        String filename;
        String contentType;

        if (doc.getStoragePath() != null && storageService.exists(doc.getStoragePath())) {
            try {
                attachmentBytes = storageService.loadAsBytes(doc.getStoragePath());
                filename = (doc.getFileName() != null && !doc.getFileName().isBlank()) ? doc.getFileName() : doc.getName();
                contentType = "application/octet-stream";
                if (filename.toLowerCase().endsWith(".pdf")) contentType = "application/pdf";
                else if (filename.toLowerCase().endsWith(".docx")) contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                else if (filename.toLowerCase().endsWith(".txt")) contentType = "text/plain";
                else if (filename.toLowerCase().endsWith(".png")) contentType = "image/png";
                else if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) contentType = "image/jpeg";
            } catch (Exception e) {
                throw new BadRequestException("Failed to load document binary from storage: " + e.getMessage());
            }
        } else {
            // Smart Fallback for ephemeral cloud storage or metadata-only records
            filename = (doc.getName() != null ? doc.getName().replaceAll("[^a-zA-Z0-9.-]", "_") : "document") + ".txt";
            contentType = "text/plain; charset=UTF-8";

            StringBuilder sb = new StringBuilder();
            sb.append("=========================================\n");
            sb.append("E2EDocs Document Export & Summary\n");
            sb.append("=========================================\n\n");
            sb.append("Document ID: ").append(doc.getId()).append("\n");
            sb.append("Title: ").append(doc.getName()).append("\n");
            sb.append("Type: ").append(doc.getType() != null ? doc.getType() : "General Document").append("\n");
            sb.append("Status: ").append(doc.getStatus()).append("\n");
            sb.append("Priority: ").append(doc.getPriority()).append("\n");
            sb.append("Department: ").append(doc.getDepartment() != null ? doc.getDepartment() : "General").append("\n");
            sb.append("Created At: ").append(doc.getCreatedAt()).append("\n\n");

            if (doc.getDescription() != null && !doc.getDescription().isBlank()) {
                sb.append("Description:\n").append(doc.getDescription()).append("\n\n");
            }

            if (doc.getExtractedText() != null && !doc.getExtractedText().isBlank()) {
                sb.append("--- Extracted Document Content ---\n").append(doc.getExtractedText()).append("\n\n");
            } else if (doc.getContentPreview() != null && !doc.getContentPreview().isBlank()) {
                sb.append("--- Document Preview Content ---\n").append(doc.getContentPreview()).append("\n\n");
            }

            sb.append("=========================================\n");
            sb.append("Generated by E2EDocs Platform • Developed by AravindRamesh\n");

            attachmentBytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }

        try {
            emailService.sendDocumentEmail(
                    request.getRecipient().trim(),
                    request.getSubject().trim(),
                    request.getMessage(),
                    filename,
                    attachmentBytes,
                    contentType
            );

            auditService.log(doc.getOrganizationId(), userId, userName, AuditAction.SENT,
                    "Document", doc.getId(), AuditStatus.SUCCESS,
                    "Sent document '" + doc.getName() + "' by email to " + request.getRecipient().trim(), "SYSTEM");
        } catch (Exception e) {
            auditService.log(doc.getOrganizationId(), userId, userName, AuditAction.SENT,
                    "Document", doc.getId(), AuditStatus.FAILURE,
                    "Failed sending document '" + doc.getName() + "' by email to " + request.getRecipient().trim() + ": " + e.getMessage(), "SYSTEM");
            throw e;
        }
    }

    @Transactional
    public DocumentResponse updateDocument(String id, DocumentUpdateRequest request, String userId, String userName) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

        if (request.getName() != null && !request.getName().isBlank()) doc.setName(request.getName());
        if (request.getDescription() != null) doc.setDescription(request.getDescription());
        if (request.getType() != null) doc.setType(request.getType());
        if (request.getStatus() != null) doc.setStatus(request.getStatus());
        if (request.getPriority() != null) doc.setPriority(request.getPriority());
        if (request.getSource() != null) doc.setSource(request.getSource());

        if (request.getOriginalSender() != null) {
            doc.setOriginalSenderEmail(request.getOriginalSender().getEmail());
            doc.setOriginalSenderName(request.getOriginalSender().getName());
        }

        if (request.getDepartment() != null) doc.setDepartment(request.getDepartment());
        if (request.getDepartmentId() != null) doc.setDepartmentId(request.getDepartmentId());
        if (request.getAssignedTo() != null) doc.setAssignedTo(request.getAssignedTo());
        if (request.getAssignedToId() != null) doc.setAssignedToId(request.getAssignedToId());
        if (request.getWorkflowId() != null) doc.setWorkflowId(request.getWorkflowId());

        if (request.getTags() != null) {
            doc.setTags(String.join(",", request.getTags()));
        }

        if (request.getMetadata() != null) {
            try {
                doc.setMetadataJson(objectMapper.writeValueAsString(request.getMetadata()));
            } catch (Exception ignored) {
            }
        }

        Document saved = documentRepository.save(doc);

        auditService.log(saved.getOrganizationId(), userId, userName, AuditAction.UPDATED,
                "Document", saved.getId(), AuditStatus.SUCCESS, "Updated document: " + saved.getName(), "SYSTEM");

        return mapToResponse(saved);
    }

    @Transactional
    public void deleteDocument(String id, String userId, String userName) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

        String orgId = doc.getOrganizationId();
        String name = doc.getName();
        String storagePath = doc.getStoragePath();

        documentRepository.delete(doc);

        if (storagePath != null) {
            storageService.delete(storagePath);
        }

        auditService.log(orgId, userId, userName, AuditAction.DELETED,
                "Document", id, AuditStatus.SUCCESS, "Deleted document: " + name, "SYSTEM");
    }

    public Resource downloadDocument(String id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

        if (doc.getStoragePath() == null) {
            throw new ResourceNotFoundException("No binary file stored for document: " + id);
        }

        return storageService.loadAsResource(doc.getStoragePath());
    }

    public DocumentResponse mapToResponse(Document doc) {
        DocumentResponse res = new DocumentResponse();
        copyToResponse(doc, res);
        return res;
    }

    private void copyToResponse(Document doc, DocumentResponse res) {
        res.setId(doc.getId());
        res.setName(doc.getName());
        res.setDescription(doc.getDescription());
        if (doc.getOriginalSenderEmail() != null || doc.getOriginalSenderName() != null) {
            res.setOriginalSender(new OriginalSenderDto(doc.getOriginalSenderEmail(), doc.getOriginalSenderName()));
        }
        res.setType(doc.getType());
        res.setStatus(doc.getStatus());
        res.setPriority(doc.getPriority());
        res.setSource(doc.getSource());
        res.setDepartment(doc.getDepartment());
        res.setDepartmentId(doc.getDepartmentId());
        res.setAssignedTo(doc.getAssignedTo());
        res.setAssignedToId(doc.getAssignedToId());
        res.setCreatedAt(doc.getCreatedAt());
        res.setUpdatedAt(doc.getUpdatedAt());
        res.setSize(doc.getSize() != null ? doc.getSize() : 0L);
        res.setRuleMatches(doc.getRuleMatches() != null ? doc.getRuleMatches() : 0);
        res.setWorkflowId(doc.getWorkflowId());

        if (doc.getTags() != null && !doc.getTags().isBlank()) {
            res.setTags(Arrays.stream(doc.getTags().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList()));
        }

        if (doc.getMetadataJson() != null && !doc.getMetadataJson().isBlank()) {
            try {
                Map<String, Object> meta = objectMapper.readValue(doc.getMetadataJson(), new TypeReference<Map<String, Object>>() {});
                res.setMetadata(meta);
            } catch (Exception ignored) {
            }
        }
    }
}
