package com.e2edocs.controller;

import com.e2edocs.dto.*;
import com.e2edocs.security.UserPrincipal;
import com.e2edocs.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<DocumentResponse>> getDocuments(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {

        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        PageResponse<DocumentResponse> response = documentService.getDocuments(
                orgId, search, status, priority, source, department, page, pageSize, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportDocuments(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {

        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        byte[] csvData = documentService.exportDocumentsCsv(
                orgId, search, status, priority, source, department, sortBy, sortOrder);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"e2edocs-documents.csv\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(csvData);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDetailResponse> getDocumentById(@PathVariable String id) {
        DocumentDetailResponse response = documentService.getDocumentById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @ModelAttribute DocumentCreateRequest request) {

        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        String userId = principal != null ? principal.getId() : "SYSTEM";
        String userName = principal != null ? principal.getName() : "System User";

        DocumentResponse response = documentService.createDocument(
                orgId, file, request, userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentResponse> createDocumentJson(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody DocumentCreateRequest request) {

        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        String userId = principal != null ? principal.getId() : "SYSTEM";
        String userName = principal != null ? principal.getName() : "System User";

        DocumentResponse response = documentService.createDocument(
                orgId, null, request, userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocumentAlias(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @ModelAttribute DocumentCreateRequest request) {

        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        String userId = principal != null ? principal.getId() : "SYSTEM";
        String userName = principal != null ? principal.getName() : "System User";

        DocumentResponse response = documentService.createDocument(
                orgId, file, request, userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BatchUploadResponse> uploadDocumentsBatch(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("files") java.util.List<MultipartFile> files,
            @ModelAttribute DocumentCreateRequest request) {

        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        String userId = principal != null ? principal.getId() : "SYSTEM";
        String userName = principal != null ? principal.getName() : "System User";

        BatchUploadResponse response = documentService.createDocumentsBatch(
                orgId, files, request, userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/send-email")
    public ResponseEntity<Map<String, Object>> sendDocumentByEmail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody DocumentSendEmailRequest request) {

        String userId = principal != null ? principal.getId() : "SYSTEM";
        String userName = principal != null ? principal.getName() : "System User";
        String callerOrgId = principal != null ? principal.getOrganizationId() : null;

        documentService.sendDocumentByEmail(id, request, userId, userName, callerOrgId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Document successfully sent via email to " + request.getRecipient()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @RequestBody DocumentUpdateRequest request) {

        String userId = principal != null ? principal.getId() : "SYSTEM";
        String userName = principal != null ? principal.getName() : "System User";

        DocumentResponse response = documentService.updateDocument(id, request, userId, userName);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {

        String userId = principal != null ? principal.getId() : "SYSTEM";
        String userName = principal != null ? principal.getName() : "System User";

        documentService.deleteDocument(id, userId, userName);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String id) {
        Resource file = documentService.downloadDocument(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }
}
