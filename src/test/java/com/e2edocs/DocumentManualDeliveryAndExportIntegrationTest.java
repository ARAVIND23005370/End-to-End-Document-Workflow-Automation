package com.e2edocs;

import com.e2edocs.dto.DocumentCreateRequest;
import com.e2edocs.dto.DocumentResponse;
import com.e2edocs.dto.DocumentSendEmailRequest;
import com.e2edocs.entity.Document;
import com.e2edocs.entity.enums.AuditAction;
import com.e2edocs.entity.enums.AuditStatus;
import com.e2edocs.entity.enums.DocumentStatus;
import com.e2edocs.entity.enums.Priority;
import com.e2edocs.exception.BadRequestException;
import com.e2edocs.exception.ResourceNotFoundException;
import com.e2edocs.repository.AuditLogRepository;
import com.e2edocs.repository.DocumentRepository;
import com.e2edocs.service.DocumentService;
import com.e2edocs.service.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DocumentManualDeliveryAndExportIntegrationTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @MockBean
    private EmailService emailService;

    private static final String ORG_ID = "org-test-delivery";
    private static final String USER_ID = "usr-test-123";
    private static final String USER_NAME = "Aravind Admin";

    private static final byte[] VALID_PDF_BYTES = "%PDF-1.4 \n1 0 obj\n<<\n/Type /Catalog\n>>\nendobj\n%%EOF".getBytes(StandardCharsets.UTF_8);

    @BeforeEach
    void setUp() {
        documentRepository.deleteAll();
        auditLogRepository.deleteAll();
    }

    @Test
    @DisplayName("Upload document stores file, extracts text, persists entity, and logs audit event")
    void testUploadDocumentEndToEnd() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Vendor_Invoice_2026.pdf",
                "application/pdf",
                VALID_PDF_BYTES
        );

        DocumentCreateRequest req = new DocumentCreateRequest();
        req.setName("Vendor Invoice 2026");
        req.setType("Invoice");
        req.setPriority(Priority.HIGH);
        req.setStatus(DocumentStatus.DRAFT);
        req.setDepartment("Finance");

        DocumentResponse res = documentService.createDocument(ORG_ID, file, req, USER_ID, USER_NAME);

        assertNotNull(res.getId());
        assertEquals("Vendor Invoice 2026", res.getName());
        assertEquals("Finance", res.getDepartment());

        // Verify entity persisted in DB
        Document savedDoc = documentRepository.findById(res.getId()).orElse(null);
        assertNotNull(savedDoc);
        assertEquals(ORG_ID, savedDoc.getOrganizationId());
        assertNotNull(savedDoc.getStoragePath());

        // Verify audit log
        boolean auditExists = auditLogRepository.findAll().stream()
                .anyMatch(a -> a.getResourceId().equals(res.getId()) && a.getAction() == AuditAction.UPLOADED);
        assertTrue(auditExists, "Audit log for upload should exist");
    }

    @Test
    @DisplayName("Export documents as CSV respects active filters and organization isolation")
    void testExportDocumentsCsvIntegration() {
        // Document in Org 1
        MockMultipartFile file1 = new MockMultipartFile("file", "invoice1.pdf", "application/pdf", VALID_PDF_BYTES);
        DocumentCreateRequest req1 = new DocumentCreateRequest();
        req1.setName("Org1 Invoice Approved");
        req1.setStatus(DocumentStatus.APPROVED);
        req1.setPriority(Priority.HIGH);
        req1.setDepartment("Finance");
        documentService.createDocument(ORG_ID, file1, req1, USER_ID, USER_NAME);

        // Document in Org 1 (Review)
        MockMultipartFile file2 = new MockMultipartFile("file", "contract1.pdf", "application/pdf", VALID_PDF_BYTES);
        DocumentCreateRequest req2 = new DocumentCreateRequest();
        req2.setName("Org1 Contract Review");
        req2.setStatus(DocumentStatus.REVIEW);
        req2.setDepartment("Legal");
        documentService.createDocument(ORG_ID, file2, req2, USER_ID, USER_NAME);

        // Document in Org 2
        MockMultipartFile file3 = new MockMultipartFile("file", "other.pdf", "application/pdf", VALID_PDF_BYTES);
        DocumentCreateRequest req3 = new DocumentCreateRequest();
        req3.setName("Other Org Document");
        documentService.createDocument("other-org-id", file3, req3, "other-usr", "Other User");

        // Export without status filter for ORG_ID
        byte[] csvAllBytes = documentService.exportDocumentsCsv(ORG_ID, null, null, null, null, null, "createdAt", "desc");
        String csvAll = new String(csvAllBytes, StandardCharsets.UTF_8);

        assertTrue(csvAll.startsWith("Document ID,Filename,Type,Source,Status,Priority,Department,Assigned User,Sender Email,Sender Name,Created At,Updated At"));
        assertTrue(csvAll.contains("Org1 Invoice Approved"));
        assertTrue(csvAll.contains("Org1 Contract Review"));
        assertFalse(csvAll.contains("Other Org Document"), "Must not leak cross-organization documents");

        // Export with search filter
        byte[] csvSearchBytes = documentService.exportDocumentsCsv(ORG_ID, "Invoice", null, null, null, null, "createdAt", "desc");
        String csvSearch = new String(csvSearchBytes, StandardCharsets.UTF_8);
        assertTrue(csvSearch.contains("Org1 Invoice Approved"));
        assertFalse(csvSearch.contains("Org1 Contract Review"));
    }

    @Test
    @DisplayName("Send document via email loads original attachment from storage and audits delivery")
    void testSendDocumentByEmailIntegration() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Financial_Report.pdf",
                "application/pdf",
                VALID_PDF_BYTES
        );

        DocumentCreateRequest req = new DocumentCreateRequest();
        req.setName("Financial Report");
        DocumentResponse doc = documentService.createDocument(ORG_ID, file, req, USER_ID, USER_NAME);

        doNothing().when(emailService).sendDocumentEmail(anyString(), anyString(), anyString(), anyString(), any(byte[].class), anyString());

        DocumentSendEmailRequest emailReq = new DocumentSendEmailRequest(
                "auditor@firm.com",
                "Annual Financial Report",
                "Please review the attached report."
        );

        documentService.sendDocumentByEmail(doc.getId(), emailReq, USER_ID, USER_NAME, ORG_ID);

        // Verify EmailService was called with original attachment bytes
        verify(emailService, times(1)).sendDocumentEmail(
                eq("auditor@firm.com"),
                eq("Annual Financial Report"),
                eq("Please review the attached report."),
                eq("Financial_Report.pdf"),
                eq(VALID_PDF_BYTES),
                eq("application/pdf")
        );

        // Verify audit log recorded successful email event
        boolean emailAudit = auditLogRepository.findAll().stream()
                .anyMatch(a -> a.getResourceId().equals(doc.getId())
                        && a.getAction() == AuditAction.SENT
                        && a.getStatus() == AuditStatus.SUCCESS
                        && a.getDetails().contains("auditor@firm.com"));
        assertTrue(emailAudit, "Audit log should record manual email dispatch");
    }

    @Test
    @DisplayName("Send document via email enforces organization isolation")
    void testSendDocumentEmailCrossOrgBlocked() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", VALID_PDF_BYTES);
        DocumentCreateRequest req = new DocumentCreateRequest();
        req.setName("Org1 Doc");
        DocumentResponse doc = documentService.createDocument(ORG_ID, file, req, USER_ID, USER_NAME);

        DocumentSendEmailRequest emailReq = new DocumentSendEmailRequest("target@firm.com", "Subject", "Message");

        // Attempting to send from another organization must fail with ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> {
            documentService.sendDocumentByEmail(doc.getId(), emailReq, "other-user", "Other User", "different-org-id");
        });
    }

    @Test
    @DisplayName("Send document via email validates recipient email format")
    void testSendDocumentEmailInvalidRecipient() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", VALID_PDF_BYTES);
        DocumentCreateRequest req = new DocumentCreateRequest();
        DocumentResponse doc = documentService.createDocument(ORG_ID, file, req, USER_ID, USER_NAME);

        DocumentSendEmailRequest emailReq = new DocumentSendEmailRequest("invalid-email-address", "Subject", "Message");

        assertThrows(BadRequestException.class, () -> {
            documentService.sendDocumentByEmail(doc.getId(), emailReq, USER_ID, USER_NAME, ORG_ID);
        });
    }
}
