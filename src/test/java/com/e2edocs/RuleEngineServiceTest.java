package com.e2edocs;

import com.e2edocs.dto.*;
import com.e2edocs.entity.*;
import com.e2edocs.entity.enums.*;
import com.e2edocs.repository.DocumentRepository;
import com.e2edocs.repository.RuleRepository;
import com.e2edocs.repository.UserRepository;
import com.e2edocs.repository.WorkflowRepository;
import com.e2edocs.service.AuditService;
import com.e2edocs.service.NotificationService;
import com.e2edocs.service.RuleEngineService;
import com.e2edocs.service.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleEngineServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditService auditService;

    private RuleEngineService ruleEngineService;

    @BeforeEach
    void setUp() {
        ruleEngineService = new RuleEngineService(
                ruleRepository,
                documentRepository,
                userRepository,
                workflowRepository,
                emailService,
                notificationService,
                auditService
        );
    }

    @Test
    @DisplayName("Evaluate condition: String equality, contains, starts_with, ends_with")
    void testConditionStringOperators() {
        Map<String, Object> context = new HashMap<>();
        context.put("document.name", "Invoice_2026_001.pdf");
        context.put("sender.email", "billing@partner.org");

        assertTrue(ruleEngineService.evaluateCondition("document.name", ConditionOperator.EQUALS, "Invoice_2026_001.pdf", context));
        assertFalse(ruleEngineService.evaluateCondition("document.name", ConditionOperator.EQUALS, "Other.pdf", context));

        assertTrue(ruleEngineService.evaluateCondition("document.name", ConditionOperator.CONTAINS, "2026", context));
        assertFalse(ruleEngineService.evaluateCondition("document.name", ConditionOperator.CONTAINS, "2025", context));

        assertTrue(ruleEngineService.evaluateCondition("document.name", ConditionOperator.STARTS_WITH, "Invoice", context));
        assertTrue(ruleEngineService.evaluateCondition("sender.email", ConditionOperator.ENDS_WITH, "@partner.org", context));
    }

    @Test
    @DisplayName("Evaluate condition: Numeric comparisons")
    void testConditionNumericOperators() {
        Map<String, Object> context = new HashMap<>();
        context.put("file.size", 2048);

        assertTrue(ruleEngineService.evaluateCondition("file.size", ConditionOperator.GREATER_THAN, "1000", context));
        assertFalse(ruleEngineService.evaluateCondition("file.size", ConditionOperator.GREATER_THAN, "3000", context));
        assertTrue(ruleEngineService.evaluateCondition("file.size", ConditionOperator.LESS_THAN, "5000", context));
    }

    @Test
    @DisplayName("Evaluate condition: IN and NOT_IN sets")
    void testConditionSetOperators() {
        Map<String, Object> context = new HashMap<>();
        context.put("metadata.source", "email");

        assertTrue(ruleEngineService.evaluateCondition("metadata.source", ConditionOperator.IN, "email, api, scanned", context));
        assertFalse(ruleEngineService.evaluateCondition("metadata.source", ConditionOperator.IN, "manual_upload, integration", context));
        assertTrue(ruleEngineService.evaluateCondition("metadata.source", ConditionOperator.NOT_IN, "manual_upload, scanned", context));
    }

    @Test
    @DisplayName("Section 1: Decision Rules - Auto Approve Document")
    void testDecisionRuleApprove() {
        Document doc = new Document("doc-dec-1", "org-1", "Contract_Verified.pdf", "Legal Document");
        doc.setExtractedText("Valid Terms and Conditions with Tax Identification Number.");

        Rule decRule = new Rule("r-dec", "org-1", "Approve Verified Contracts", "Approves contracts", RuleStatus.ACTIVE, RuleType.DECISION, 1);
        RuleConditionGroup group = new RuleConditionGroup("cg-1", decRule, ConditionLogic.AND, 0);
        group.addCondition(new RuleCondition("c-1", group, "extracted.text", ConditionOperator.CONTAINS, "Tax Identification", 0));
        decRule.addConditionGroup(group);
        decRule.addAction(new RuleAction("act-1", decRule, ActionType.SET_DECISION, "approved", null, 0));

        when(ruleRepository.findByOrganizationIdAndStatusOrderByEvaluationOrderAsc("org-1", RuleStatus.ACTIVE))
                .thenReturn(Collections.singletonList(decRule));

        ruleEngineService.evaluateDocument(doc);

        assertEquals(DocumentStatus.APPROVED, doc.getStatus());
        assertTrue(doc.getDecisionReason().contains("approved"));
        verify(documentRepository, times(1)).save(doc);
    }

    @Test
    @DisplayName("Section 1: Decision Rules - Auto Reject & Capture Missing Fields")
    void testDecisionRuleMissingFieldsTracking() {
        Document doc = new Document("doc-dec-2", "org-1", "Incomplete_Doc.pdf", "General");
        doc.setExtractedText("Missing all required metadata.");

        Rule decRule = new Rule("r-dec-2", "org-1", "Strict Rejection on Missing ID", "Requires ID", RuleStatus.ACTIVE, RuleType.DECISION, 1);
        RuleConditionGroup group = new RuleConditionGroup("cg-2", decRule, ConditionLogic.AND, 0);
        group.addCondition(new RuleCondition("c-2", group, "extracted.text", ConditionOperator.CONTAINS, "Government ID", 0));
        decRule.addConditionGroup(group);
        decRule.addAction(new RuleAction("act-2", decRule, ActionType.SET_DECISION, "rejected", null, 0));

        when(ruleRepository.findByOrganizationIdAndStatusOrderByEvaluationOrderAsc("org-1", RuleStatus.ACTIVE))
                .thenReturn(Collections.singletonList(decRule));

        ruleEngineService.evaluateDocument(doc);

        // Document should not match -> condition fails -> missing fields tracked
        assertNotNull(doc.getMissingFields());
        assertTrue(doc.getMissingFields().contains("Government ID"));
    }

    @Test
    @DisplayName("Section 2: Folder Rules - Classify into Virtual Category / Folder")
    void testFolderRuleClassification() {
        Document doc = new Document("doc-fld-1", "org-1", "Power_Outage_Report.pdf", "Unclassified");
        doc.setExtractedText("Electricity power line outage reported in Sector 4.");

        Rule folderRule = new Rule("r-fld", "org-1", "Classify Electricity Docs", "Classifies into Electricity folder", RuleStatus.ACTIVE, RuleType.FOLDER, 1);
        RuleConditionGroup group = new RuleConditionGroup("cg-fld", folderRule, ConditionLogic.OR, 0);
        group.addCondition(new RuleCondition("c-f1", group, "extracted.text", ConditionOperator.CONTAINS, "Electricity power", 0));
        folderRule.addConditionGroup(group);
        folderRule.addAction(new RuleAction("act-f1", folderRule, ActionType.ASSIGN_FOLDER, "Electricity & Utilities", null, 0));

        when(ruleRepository.findByOrganizationIdAndStatusOrderByEvaluationOrderAsc("org-1", RuleStatus.ACTIVE))
                .thenReturn(Collections.singletonList(folderRule));

        ruleEngineService.evaluateDocument(doc);

        assertEquals("Electricity & Utilities", doc.getType());
        verify(documentRepository, times(1)).save(doc);
    }

    @Test
    @DisplayName("Section 3: Sorting Rules - Set Document Sorting Priority (Critical / High / Medium / Low)")
    void testSortingRulePriority() {
        Document doc = new Document("doc-srt-1", "org-1", "Urgent_Investigation.pdf", "Audit");
        doc.setPriority(Priority.LOW);
        doc.setExtractedText("CRITICAL SECURITY BREACH ALERT");

        Rule sortRule = new Rule("r-srt", "org-1", "Critical Priority Rule", "Sets critical priority", RuleStatus.ACTIVE, RuleType.SORTING, 1);
        RuleConditionGroup group = new RuleConditionGroup("cg-srt", sortRule, ConditionLogic.AND, 0);
        group.addCondition(new RuleCondition("c-s1", group, "extracted.text", ConditionOperator.CONTAINS, "SECURITY BREACH", 0));
        sortRule.addConditionGroup(group);
        sortRule.addAction(new RuleAction("act-s1", sortRule, ActionType.SET_PRIORITY, "critical", null, 0));

        when(ruleRepository.findByOrganizationIdAndStatusOrderByEvaluationOrderAsc("org-1", RuleStatus.ACTIVE))
                .thenReturn(Collections.singletonList(sortRule));

        ruleEngineService.evaluateDocument(doc);

        assertEquals(Priority.CRITICAL, doc.getPriority());
        verify(documentRepository, times(1)).save(doc);
    }

    @Test
    @DisplayName("Section 4: Routing Rules - Assign to Department / User")
    void testRoutingRuleAssignment() {
        Document doc = new Document("doc-rout-1", "org-1", "Legal_Review.pdf", "Legal");

        Rule routeRule = new Rule("r-rout", "org-1", "Route To Legal", "Assigns Legal Department", RuleStatus.ACTIVE, RuleType.ROUTING, 1);
        RuleConditionGroup group = new RuleConditionGroup("cg-rout", routeRule, ConditionLogic.AND, 0);
        group.addCondition(new RuleCondition("c-r1", group, "document.name", ConditionOperator.CONTAINS, "Legal", 0));
        routeRule.addConditionGroup(group);
        routeRule.addAction(new RuleAction("act-r1", routeRule, ActionType.ASSIGN_DEPARTMENT, "Legal & Compliance", null, 0));

        when(ruleRepository.findByOrganizationIdAndStatusOrderByEvaluationOrderAsc("org-1", RuleStatus.ACTIVE))
                .thenReturn(Collections.singletonList(routeRule));

        ruleEngineService.evaluateDocument(doc);

        assertEquals("Legal & Compliance", doc.getDepartment());
        verify(documentRepository, times(1)).save(doc);
    }

    @Test
    @DisplayName("Section 5: Communication Rules - Render Dynamic Template Variables")
    void testCommunicationRuleTemplateInterpolation() {
        Document doc = new Document("doc-comm-1", "org-1", "Scholarship_App.pdf", "Scholarship");
        doc.setStatus(DocumentStatus.APPROVED);
        doc.setOriginalSenderEmail("student@university.edu");
        doc.setDecisionReason("Passed all academic criteria");

        Rule commRule = new Rule("r-comm", "org-1", "Approval Email", "Sends approval notice", RuleStatus.ACTIVE, RuleType.COMMUNICATION, 1);
        RuleConditionGroup group = new RuleConditionGroup("cg-comm", commRule, ConditionLogic.AND, 0);
        group.addCondition(new RuleCondition("c-c1", group, "document.status", ConditionOperator.EQUALS, "approved", 0));
        commRule.addConditionGroup(group);

        String emailConfigJson = "{\"recipientType\":\"ORIGINAL_SENDER\",\"subject\":\"Approved: {{document_name}}\",\"message\":\"Hello, your document {{document_name}} (ID: {{document_id}}) decision is {{decision}}.\"}";
        commRule.addAction(new RuleAction("act-c1", commRule, ActionType.SEND_EMAIL, "ORIGINAL_SENDER", emailConfigJson, 0));

        when(ruleRepository.findByOrganizationIdAndStatusOrderByEvaluationOrderAsc("org-1", RuleStatus.ACTIVE))
                .thenReturn(Collections.singletonList(commRule));

        ruleEngineService.evaluateDocument(doc);

        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailService, times(1)).sendEmail(
                eq("student@university.edu"),
                subjectCaptor.capture(),
                bodyCaptor.capture(),
                anyMap()
        );

        assertTrue(subjectCaptor.getValue().contains("Scholarship_App.pdf"));
        assertTrue(bodyCaptor.getValue().contains("APPROVED"));
    }
}
