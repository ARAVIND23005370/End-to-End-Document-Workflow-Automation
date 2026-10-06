package com.e2edocs;

import com.e2edocs.dto.*;
import com.e2edocs.entity.Document;
import com.e2edocs.entity.enums.*;
import com.e2edocs.repository.AuditLogRepository;
import com.e2edocs.repository.DocumentRepository;
import com.e2edocs.repository.OrganizationRepository;
import com.e2edocs.service.DocumentService;
import com.e2edocs.service.RuleEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PipelineValidationIntegrationTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private RuleEngineService ruleEngineService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private final String orgId = "org-test-pipeline";

    @BeforeEach
    void setUp() {
        if (!organizationRepository.existsById(orgId)) {
            organizationRepository.save(new com.e2edocs.entity.Organization(
                    orgId, "Universal Test Org", "UTO", "Generic Workspace", "test.org"
            ));
        }

        // Configure Section-Isolated Rules
        // 1. Decision Rule - Approve when Tax ID present
        RuleInputDto approveRule = new RuleInputDto();
        approveRule.setName("Approve Valid Tax Documents");
        approveRule.setRuleType(RuleType.DECISION);
        approveRule.setStatus(RuleStatus.ACTIVE);
        approveRule.setEvaluationOrder(1);
        RuleConditionGroupDto approveGroup = new RuleConditionGroupDto();
        approveGroup.setLogic(ConditionLogic.OR);
        approveGroup.getConditions().add(new RuleConditionDto("c-app-1", "extracted.text", ConditionOperator.CONTAINS, "Tax ID"));
        approveRule.getConditionGroups().add(approveGroup);
        approveRule.getActions().add(new RuleActionDto("act-1", ActionType.SET_DECISION, "approved", null));
        ruleEngineService.createRule(orgId, approveRule, "Admin");

        // 2. Decision Rule - Manual Review for Drafts
        RuleInputDto reviewRule = new RuleInputDto();
        reviewRule.setName("Flag Inquiries for Review");
        reviewRule.setRuleType(RuleType.DECISION);
        reviewRule.setStatus(RuleStatus.ACTIVE);
        reviewRule.setEvaluationOrder(2);
        RuleConditionGroupDto reviewGroup = new RuleConditionGroupDto();
        reviewGroup.setLogic(ConditionLogic.AND);
        reviewGroup.getConditions().add(new RuleConditionDto("c-rev-1", "extracted.text", ConditionOperator.CONTAINS, "DRAFT"));
        reviewRule.getConditionGroups().add(reviewGroup);
        reviewRule.getActions().add(new RuleActionDto("act-2", ActionType.SET_DECISION, "review", null));
        ruleEngineService.createRule(orgId, reviewRule, "Admin");

        // 3. Folder Rule - Classify Invoices
        RuleInputDto folderRule = new RuleInputDto();
        folderRule.setName("Classify Commercial Invoices");
        folderRule.setRuleType(RuleType.FOLDER);
        folderRule.setStatus(RuleStatus.ACTIVE);
        folderRule.setEvaluationOrder(1);
        RuleConditionGroupDto fGroup = new RuleConditionGroupDto();
        fGroup.setLogic(ConditionLogic.OR);
        fGroup.getConditions().add(new RuleConditionDto("c-f-1", "extracted.text", ConditionOperator.CONTAINS, "COMMERCIAL INVOICE"));
        folderRule.getConditionGroups().add(fGroup);
        folderRule.getActions().add(new RuleActionDto("act-3", ActionType.ASSIGN_FOLDER, "Invoices", null));
        ruleEngineService.createRule(orgId, folderRule, "Admin");

        // 4. Sorting Rule - Critical priority for urgent keyword
        RuleInputDto sortRule = new RuleInputDto();
        sortRule.setName("Urgent Priority Escalation");
        sortRule.setRuleType(RuleType.SORTING);
        sortRule.setStatus(RuleStatus.ACTIVE);
        sortRule.setEvaluationOrder(1);
        RuleConditionGroupDto sGroup = new RuleConditionGroupDto();
        sGroup.setLogic(ConditionLogic.OR);
        sGroup.getConditions().add(new RuleConditionDto("c-s-1", "extracted.text", ConditionOperator.CONTAINS, "Urgent"));
        sortRule.getConditionGroups().add(sGroup);
        sortRule.getActions().add(new RuleActionDto("act-4", ActionType.SET_PRIORITY, "critical", null));
        ruleEngineService.createRule(orgId, sortRule, "Admin");

        // 5. Routing Rule - Assign to Billing
        RuleInputDto routeRule = new RuleInputDto();
        routeRule.setName("Route Invoices to Billing");
        routeRule.setRuleType(RuleType.ROUTING);
        routeRule.setStatus(RuleStatus.ACTIVE);
        routeRule.setEvaluationOrder(1);
        RuleConditionGroupDto rGroup = new RuleConditionGroupDto();
        rGroup.setLogic(ConditionLogic.AND);
        rGroup.getConditions().add(new RuleConditionDto("c-r-1", "extracted.text", ConditionOperator.CONTAINS, "COMMERCIAL INVOICE"));
        routeRule.getConditionGroups().add(rGroup);
        routeRule.getActions().add(new RuleActionDto("act-5", ActionType.ASSIGN_DEPARTMENT, "Billing Operations", null));
        ruleEngineService.createRule(orgId, routeRule, "Admin");

        // 6. Communication Rule - Send notification for approved
        RuleInputDto commRule = new RuleInputDto();
        commRule.setName("Approval Email Notification");
        commRule.setRuleType(RuleType.COMMUNICATION);
        commRule.setStatus(RuleStatus.ACTIVE);
        commRule.setEvaluationOrder(1);
        RuleConditionGroupDto cGroup = new RuleConditionGroupDto();
        cGroup.setLogic(ConditionLogic.AND);
        cGroup.getConditions().add(new RuleConditionDto("c-c-1", "extracted.text", ConditionOperator.CONTAINS, "Tax ID"));
        commRule.getConditionGroups().add(cGroup);
        EmailConfigDto emailConfig = new EmailConfigDto();
        emailConfig.setRecipientType(EmailRecipientType.ORIGINAL_SENDER);
        emailConfig.setSubject("Document Approved: {{document_name}}");
        emailConfig.setMessage("Document {{document_id}} has been marked {{decision}} in category {{category}}.");
        commRule.getActions().add(new RuleActionDto("act-6", ActionType.SEND_EMAIL, "original_sender", emailConfig));
        ruleEngineService.createRule(orgId, commRule, "Admin");
    }

    @Test
    @DisplayName("Single Document Ingestion: Complete 5-Stage Pipeline Execution")
    void testSingleDocumentCompletePipeline() {
        String content = "COMMERCIAL INVOICE - INV-2026-001\nTax ID: TX-8821\nUrgent processing requested.\nTotal: $1,200.00";
        MockMultipartFile file = new MockMultipartFile(
                "file", "Invoice_001.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8)
        );

        DocumentCreateRequest req = new DocumentCreateRequest();
        req.setOriginalSenderEmail("billing@partner.org");

        DocumentResponse res = documentService.createDocument(orgId, file, req, "u-test", "Test User");

        assertNotNull(res.getId());
        assertEquals("Invoice_001.txt", res.getName());

        Document doc = documentRepository.findById(res.getId()).orElseThrow();

        // 1. Text Extraction Verification
        assertNotNull(doc.getExtractedText());
        assertTrue(doc.getExtractedText().contains("Tax ID: TX-8821"));

        // 2. Decision Rules Verification -> APPROVED
        assertEquals(DocumentStatus.APPROVED, doc.getStatus());

        // 3. Folder Rules Verification -> Invoices
        assertEquals("Invoices", doc.getType());

        // 4. Sorting Rules Verification -> CRITICAL (from "Urgent")
        assertEquals(Priority.CRITICAL, doc.getPriority());

        // 5. Routing Rules Verification -> Billing Operations
        assertEquals("Billing Operations", doc.getDepartment());

        // 6. Audit Log Verification
        assertFalse(auditLogRepository.findByOrganizationId(orgId).isEmpty());
    }

    @Test
    @DisplayName("Manual Review Pipeline: Review Flag, Reason Storage, and Manual Approval")
    void testManualReviewAndApproveFlow() {
        String content = "DOCUMENT SUBMISSION DRAFT\nApplicant: Alex Morgan\nGeneral Inquiry.";
        MockMultipartFile file = new MockMultipartFile(
                "file", "Draft_Inquiry.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8)
        );

        DocumentResponse res = documentService.createDocument(orgId, file, new DocumentCreateRequest(), "u-test", "Test User");

        Document doc = documentRepository.findById(res.getId()).orElseThrow();

        // Should match manual review rule
        assertEquals(DocumentStatus.REVIEW, doc.getStatus());

        // Reviewer manually approves
        DocumentUpdateRequest updateReq = new DocumentUpdateRequest();
        updateReq.setStatus(DocumentStatus.APPROVED);
        documentService.updateDocument(doc.getId(), updateReq, "reviewer-1", "Lead Reviewer");

        Document approvedDoc = documentRepository.findById(doc.getId()).orElseThrow();
        assertEquals(DocumentStatus.APPROVED, approvedDoc.getStatus());
    }

    @Test
    @DisplayName("Batch Document Ingestion: Multiple Files Processed Independently")
    void testBatchIngestionIndependentProcessing() {
        String doc1Content = "COMMERCIAL INVOICE\nTax ID: TX-991";
        String doc2Content = "DOCUMENT SUBMISSION DRAFT\nApplicant: Jane Doe";

        MockMultipartFile file1 = new MockMultipartFile("files", "Doc1_Invoice.txt", "text/plain", doc1Content.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile file2 = new MockMultipartFile("files", "Doc2_Draft.txt", "text/plain", doc2Content.getBytes(StandardCharsets.UTF_8));

        BatchUploadResponse batchRes = documentService.createDocumentsBatch(
                orgId, Arrays.asList(file1, file2), new DocumentCreateRequest(), "u-test", "Test User"
        );

        assertEquals(2, batchRes.getTotalFiles());
        assertEquals(2, batchRes.getSuccessful());
        assertEquals(0, batchRes.getFailed());

        Document d1 = documentRepository.findById(batchRes.getDocuments().get(0).getId()).orElseThrow();
        Document d2 = documentRepository.findById(batchRes.getDocuments().get(1).getId()).orElseThrow();

        // Document 1 got APPROVED and assigned to Invoices
        assertEquals(DocumentStatus.APPROVED, d1.getStatus());
        assertEquals("Invoices", d1.getType());

        // Document 2 got REVIEW because it is a draft
        assertEquals(DocumentStatus.REVIEW, d2.getStatus());
    }

    @Test
    @DisplayName("Section Isolation: Evaluation Order does not affect Sorting Priority")
    void testEvaluationOrderDoesNotChangeSortingPriority() {
        String content = "GENERAL NOTE MEMO\nStandard informational memo without priority tags.";
        MockMultipartFile file = new MockMultipartFile("file", "Note.txt", "text/plain", content.getBytes(StandardCharsets.UTF_8));

        DocumentCreateRequest req = new DocumentCreateRequest();
        req.setPriority(Priority.LOW);

        DocumentResponse res = documentService.createDocument(orgId, file, req, "u-test", "Test User");
        Document doc = documentRepository.findById(res.getId()).orElseThrow();

        // Even though rules have evaluationOrder = 1 and 2, document priority remains LOW because Sorting rule didn't match
        assertEquals(Priority.LOW, doc.getPriority());
    }
}
