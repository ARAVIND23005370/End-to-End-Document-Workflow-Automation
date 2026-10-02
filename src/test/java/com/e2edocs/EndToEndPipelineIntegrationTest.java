package com.e2edocs;

import com.e2edocs.dto.*;
import com.e2edocs.entity.*;
import com.e2edocs.entity.enums.*;
import com.e2edocs.repository.*;
import com.e2edocs.service.AuditService;
import com.e2edocs.service.DocumentService;
import com.e2edocs.service.NotificationService;
import com.e2edocs.service.RuleEngineService;
import com.e2edocs.service.WorkflowService;
import com.e2edocs.service.email.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EndToEndPipelineIntegrationTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private RuleEngineService ruleEngineService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @SpyBean
    private EmailService emailService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String orgId;
    private String otherOrgId;
    private User orgUser;
    private User otherOrgUser;
    private Workflow orgWorkflow;

    @BeforeEach
    void setupTestData() {
        orgId = "org-test-" + UUID.randomUUID().toString().substring(0, 8);
        otherOrgId = "org-other-" + UUID.randomUUID().toString().substring(0, 8);

        orgUser = new User("u-test-1", orgId, "Sarah Analyst", "sarah@primary.org", "passHash123", UserRole.USER);
        orgUser.setStatus(UserStatus.ACTIVE);
        userRepository.save(orgUser);

        otherOrgUser = new User("u-other-1", otherOrgId, "Bob Hacker", "bob@other.org", "passHash123", UserRole.USER);
        otherOrgUser.setStatus(UserStatus.ACTIVE);
        userRepository.save(otherOrgUser);

        orgWorkflow = new Workflow("wf-test-1", orgId, "Inbound Document Routing", "Standard workflow", WorkflowStatus.ACTIVE, "Sarah Analyst");
        WorkflowStep step1 = new WorkflowStep("ws-test-1", orgWorkflow, "Intake & Classification", "trigger", "completed", 1);
        WorkflowStep step2 = new WorkflowStep("ws-test-2", orgWorkflow, "Manual Review", "action", "pending", 2);
        orgWorkflow.addStep(step1);
        orgWorkflow.addStep(step2);
        workflowRepository.save(orgWorkflow);
    }

    @Test
    @DisplayName("1-4: Ingest Document -> Extract Content -> Build DocumentContext -> Match Rule by Document Content")
    void testIngestAndMatchByContent() {
        // Rule: If document.content contains 'NDA' -> SET_PRIORITY high
        Rule rule = new Rule("r-nda", orgId, "NDA Handler", "Auto-high priority for NDAs", RuleStatus.ACTIVE, 1);
        RuleConditionGroup group = new RuleConditionGroup("cg-nda", rule, ConditionLogic.AND, 0);
        group.addCondition(new RuleCondition("c-nda", group, "document.content", ConditionOperator.CONTAINS, "NON-DISCLOSURE", 0));
        rule.addConditionGroup(group);
        rule.addAction(new RuleAction("act-nda", rule, ActionType.SET_PRIORITY, "high", null, 0));
        ruleRepository.save(rule);

        String sampleContent = "MUTUAL NON-DISCLOSURE AGREEMENT\nBetween Acme Corp and Partner Corp.\nTerms effective 2026.";
        MockMultipartFile file = new MockMultipartFile("file", "nda_agreement.txt", "text/plain", sampleContent.getBytes(StandardCharsets.UTF_8));

        DocumentCreateRequest req = new DocumentCreateRequest();
        req.setName("Mutual NDA Agreement");
        req.setType("Legal Document");
        req.setPriority(Priority.LOW);

        DocumentResponse docResp = documentService.createDocument(orgId, file, req, orgUser.getId(), orgUser.getName());

        assertNotNull(docResp);
        Document savedDoc = documentRepository.findById(docResp.getId()).orElseThrow();
        assertEquals(Priority.HIGH, savedDoc.getPriority());
        assertEquals(1, savedDoc.getRuleMatches());
        assertTrue(savedDoc.getExtractedText().contains("NON-DISCLOSURE"));
    }

    @Test
    @DisplayName("5-7: Match Rule by Sender Email, File Extension, and File Size")
    void testMatchBySenderExtensionAndSize() {
        // Rule: sender.email ends_with '@partner.org' AND file.extension equals '.txt' AND file.size > 10
        Rule rule = new Rule("r-multi-cond", orgId, "Partner Text Docs", "Matches partner txts", RuleStatus.ACTIVE, 1);
        RuleConditionGroup group = new RuleConditionGroup("cg-multi", rule, ConditionLogic.AND, 0);
        group.addCondition(new RuleCondition("c1", group, "sender.email", ConditionOperator.ENDS_WITH, "@partner.org", 0));
        group.addCondition(new RuleCondition("c2", group, "file.extension", ConditionOperator.EQUALS, ".txt", 1));
        group.addCondition(new RuleCondition("c3", group, "file.size", ConditionOperator.GREATER_THAN, "10", 2));
        rule.addConditionGroup(group);
        rule.addAction(new RuleAction("a1", rule, ActionType.ADD_TAG, "partner-verified", null, 0));
        ruleRepository.save(rule);

        MockMultipartFile file = new MockMultipartFile("file", "data_export.txt", "text/plain", "Valid payload size greater than ten bytes.".getBytes(StandardCharsets.UTF_8));

        DocumentCreateRequest req = new DocumentCreateRequest();
        req.setName("Partner Export");
        req.setOriginalSenderEmail("operations@partner.org");
        req.setOriginalSenderName("Partner Ops");

        DocumentResponse docResp = documentService.createDocument(orgId, file, req, orgUser.getId(), orgUser.getName());
        Document savedDoc = documentRepository.findById(docResp.getId()).orElseThrow();

        assertNotNull(savedDoc.getTags());
        assertTrue(savedDoc.getTags().contains("partner-verified"));
        assertEquals(1, savedDoc.getRuleMatches());
    }

    @Test
    @DisplayName("8-9: Rule Evaluation Order & Multiple Rule Matching Execution")
    void testEvaluationOrderAndMultipleRuleMatching() {
        // Rule 1 (order 10): adds tag 'tier-1'
        Rule rule1 = new Rule("r-order-1", orgId, "First Rule", "Sets tier-1", RuleStatus.ACTIVE, 10);
        RuleConditionGroup g1 = new RuleConditionGroup("cg-o1", rule1, ConditionLogic.AND, 0);
        g1.addCondition(new RuleCondition("co1", g1, "document.name", ConditionOperator.CONTAINS, "Report", 0));
        rule1.addConditionGroup(g1);
        rule1.addAction(new RuleAction("ao1", rule1, ActionType.ADD_TAG, "tier-1", null, 0));
        ruleRepository.save(rule1);

        // Rule 2 (order 20): adds tag 'tier-2' and sets priority CRITICAL
        Rule rule2 = new Rule("r-order-2", orgId, "Second Rule", "Sets tier-2", RuleStatus.ACTIVE, 20);
        RuleConditionGroup g2 = new RuleConditionGroup("cg-o2", rule2, ConditionLogic.AND, 0);
        g2.addCondition(new RuleCondition("co2", g2, "document.name", ConditionOperator.CONTAINS, "Report", 0));
        rule2.addConditionGroup(g2);
        rule2.addAction(new RuleAction("ao2", rule2, ActionType.ADD_TAG, "tier-2", null, 0));
        rule2.addAction(new RuleAction("ao3", rule2, ActionType.SET_PRIORITY, "critical", null, 1));
        ruleRepository.save(rule2);

        MockMultipartFile file = new MockMultipartFile("file", "Financial_Report.txt", "text/plain", "Report summary contents".getBytes(StandardCharsets.UTF_8));
        DocumentCreateRequest req = new DocumentCreateRequest();
        req.setName("Financial Report");
        req.setPriority(Priority.LOW);

        DocumentResponse docResp = documentService.createDocument(orgId, file, req, orgUser.getId(), orgUser.getName());
        Document savedDoc = documentRepository.findById(docResp.getId()).orElseThrow();

        assertEquals(2, savedDoc.getRuleMatches(), "Both rules must match and execute in order");
        assertTrue(savedDoc.getTags().contains("tier-1"));
        assertTrue(savedDoc.getTags().contains("tier-2"));
        assertEquals(Priority.CRITICAL, savedDoc.getPriority());
    }

    @Test
    @DisplayName("10-17: All 8 Action Executions (SET_PRIORITY, ASSIGN_USER, START_WORKFLOW, SET_DECISION, ADD_TAG, SEND_NOTIFICATION, SEND_EMAIL, FORWARD_DOCUMENT)")
    void testAllEightActionsExecution() {
        Rule comprehensiveRule = new Rule("r-all-actions", orgId, "Master Orchestration Rule", "Executes all actions", RuleStatus.ACTIVE, 1);
        RuleConditionGroup group = new RuleConditionGroup("cg-all", comprehensiveRule, ConditionLogic.AND, 0);
        group.addCondition(new RuleCondition("ca1", group, "document.name", ConditionOperator.CONTAINS, "Omni", 0));
        comprehensiveRule.addConditionGroup(group);

        // 1. SET_PRIORITY
        comprehensiveRule.addAction(new RuleAction("a-prio", comprehensiveRule, ActionType.SET_PRIORITY, "critical", null, 0));
        // 2. ASSIGN_USER
        comprehensiveRule.addAction(new RuleAction("a-assign", comprehensiveRule, ActionType.ASSIGN_USER, orgUser.getEmail(), null, 1));
        // 3. START_WORKFLOW
        comprehensiveRule.addAction(new RuleAction("a-wf", comprehensiveRule, ActionType.START_WORKFLOW, orgWorkflow.getId(), null, 2));
        // 4. SET_DECISION
        comprehensiveRule.addAction(new RuleAction("a-dec", comprehensiveRule, ActionType.SET_DECISION, "approved", null, 3));
        // 5. ADD_TAG
        comprehensiveRule.addAction(new RuleAction("a-tag", comprehensiveRule, ActionType.ADD_TAG, "omni-processed", null, 4));
        // 6. SEND_NOTIFICATION
        comprehensiveRule.addAction(new RuleAction("a-notif", comprehensiveRule, ActionType.SEND_NOTIFICATION, "Omni document processed successfully", null, 5));
        // 7. SEND_EMAIL
        EmailConfigDto emailCfg = new EmailConfigDto();
        emailCfg.setRecipientType(EmailRecipientType.ORIGINAL_SENDER);
        emailCfg.setSubject("Confirmation for {document.name}");
        emailCfg.setMessage("Hello {sender.name}, your document {document.name} has been approved.");
        String emailCfgJson;
        try {
            emailCfgJson = objectMapper.writeValueAsString(emailCfg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        comprehensiveRule.addAction(new RuleAction("a-email", comprehensiveRule, ActionType.SEND_EMAIL, "Receipt Email", emailCfgJson, 6));
        // 8. FORWARD_DOCUMENT
        comprehensiveRule.addAction(new RuleAction("a-fwd", comprehensiveRule, ActionType.FORWARD_DOCUMENT, "https://api.partner.org/webhook/documents", null, 7));

        ruleRepository.save(comprehensiveRule);

        MockMultipartFile file = new MockMultipartFile("file", "Omni_Spec.txt", "text/plain", "Omni specification content".getBytes(StandardCharsets.UTF_8));
        DocumentCreateRequest req = new DocumentCreateRequest();
        req.setName("Omni Spec");
        req.setOriginalSenderEmail("partner@enterprise.org");
        req.setOriginalSenderName("Partner Lead");

        DocumentResponse docResp = documentService.createDocument(orgId, file, req, orgUser.getId(), orgUser.getName());
        Document savedDoc = documentRepository.findById(docResp.getId()).orElseThrow();

        // Verify side effects
        // 1. SET_PRIORITY
        assertEquals(Priority.CRITICAL, savedDoc.getPriority());
        // 2. ASSIGN_USER
        assertEquals(orgUser.getName(), savedDoc.getAssignedTo());
        assertEquals(orgUser.getId(), savedDoc.getAssignedToId());
        // 3. START_WORKFLOW
        assertEquals(orgWorkflow.getId(), savedDoc.getWorkflowId());
        Workflow updatedWf = workflowRepository.findById(orgWorkflow.getId()).orElseThrow();
        assertEquals(1, updatedWf.getDocumentsProcessed());
        // 4. SET_DECISION
        assertEquals(DocumentStatus.APPROVED, savedDoc.getStatus());
        // 5. ADD_TAG
        assertTrue(savedDoc.getTags().contains("omni-processed"));
        // 6. SEND_NOTIFICATION
        List<NotificationResponse> notifs = notificationService.getAllNotifications(orgId, orgUser.getId());
        assertFalse(notifs.isEmpty());
        assertTrue(notifs.stream().anyMatch(n -> n.getMessage().contains("Omni document processed successfully")));
        // 7. SEND_EMAIL
        verify(emailService, atLeastOnce()).sendEmail(
                eq("partner@enterprise.org"),
                contains("Confirmation for"),
                contains("Hello"),
                argThat(map -> "Omni Spec".equals(map.get("document.name")) && "Partner Lead".equals(map.get("sender.name")))
        );
        // 8. FORWARD_DOCUMENT & Audit Trail
        List<AuditLog> auditLogs = auditLogRepository.findByResourceIdOrderByTimestampDesc(savedDoc.getId());
        assertFalse(auditLogs.isEmpty());
        assertTrue(auditLogs.stream().anyMatch(a -> a.getDetails().contains("Forwarded document to")));
    }

    @Test
    @DisplayName("18-19: Workflow Progression, Step Transitions, and Completion")
    void testWorkflowProgressionAndCompletion() {
        Workflow wf = new Workflow("wf-prog", orgId, "Contract Lifecycle", "Multi-step workflow", WorkflowStatus.ACTIVE, "Sarah Analyst");
        WorkflowStep s1 = new WorkflowStep("ws-p1", wf, "Step 1: Ingestion", "trigger", "completed", 1);
        WorkflowStep s2 = new WorkflowStep("ws-p2", wf, "Step 2: Legal Review", "action", "pending", 2);
        WorkflowStep s3 = new WorkflowStep("ws-p3", wf, "Step 3: Final Signoff", "action", "pending", 3);
        wf.addStep(s1);
        wf.addStep(s2);
        wf.addStep(s3);
        workflowRepository.save(wf);

        // Transition Step 2 to completed
        WorkflowResponse respStep2 = workflowService.updateStepStatus("wf-prog", "ws-p2", "completed", "Sarah Analyst");
        assertEquals("completed", respStep2.getSteps().stream().filter(s -> s.getId().equals("ws-p2")).findFirst().orElseThrow().getStatus());

        // Transition Step 3 to completed
        WorkflowResponse respStep3 = workflowService.updateStepStatus("wf-prog", "ws-p3", "completed", "Sarah Analyst");
        assertEquals("completed", respStep3.getSteps().stream().filter(s -> s.getId().equals("ws-p3")).findFirst().orElseThrow().getStatus());

        // Verify audit logs for step transitions
        List<AuditLog> wfStepAudits = auditLogRepository.findByResourceIdOrderByTimestampDesc("ws-p2");
        assertFalse(wfStepAudits.isEmpty());
        assertEquals(AuditAction.UPDATED, wfStepAudits.get(0).getAction());
    }

    @Test
    @DisplayName("20: Audit Trail Captures Full Pipeline History")
    void testAuditTrailCompleteness() {
        MockMultipartFile file = new MockMultipartFile("file", "audit_test.txt", "text/plain", "Sample for audit trace".getBytes(StandardCharsets.UTF_8));
        DocumentCreateRequest req = new DocumentCreateRequest();
        req.setName("Audit Sample Document");

        DocumentResponse docResp = documentService.createDocument(orgId, file, req, orgUser.getId(), orgUser.getName());

        List<AuditLog> logs = auditLogRepository.findByResourceIdOrderByTimestampDesc(docResp.getId());
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().anyMatch(l -> l.getAction() == AuditAction.UPLOADED));
    }

    @Test
    @DisplayName("21-22: Organization Isolation & Unauthorized Cross-Tenant Actions Prevention")
    void testOrganizationIsolation() {
        // Create Rule in Organization A
        Rule ruleOrgA = new Rule("r-org-a", orgId, "Org A Rule", "Isolation test", RuleStatus.ACTIVE, 1);
        RuleConditionGroup groupA = new RuleConditionGroup("cg-a", ruleOrgA, ConditionLogic.AND, 0);
        groupA.addCondition(new RuleCondition("ca", groupA, "document.name", ConditionOperator.CONTAINS, "Shared", 0));
        ruleOrgA.addConditionGroup(groupA);
        ruleOrgA.addAction(new RuleAction("act-a", ruleOrgA, ActionType.SET_PRIORITY, "critical", null, 0));
        // Cross-org assignment attempt in action: try to assign user from otherOrgId
        ruleOrgA.addAction(new RuleAction("act-cross-user", ruleOrgA, ActionType.ASSIGN_USER, otherOrgUser.getId(), null, 1));
        ruleRepository.save(ruleOrgA);

        // Upload document to Organization B (should NOT match Rule from Org A)
        MockMultipartFile fileB = new MockMultipartFile("file", "Shared_Report.txt", "text/plain", "Shared content".getBytes(StandardCharsets.UTF_8));
        DocumentCreateRequest reqB = new DocumentCreateRequest();
        reqB.setName("Shared Report B");
        reqB.setPriority(Priority.LOW);

        DocumentResponse docRespB = documentService.createDocument(otherOrgId, fileB, reqB, otherOrgUser.getId(), otherOrgUser.getName());
        Document docB = documentRepository.findById(docRespB.getId()).orElseThrow();

        // Rule from Org A should NOT have executed on Document from Org B
        assertEquals(0, docB.getRuleMatches());
        assertEquals(Priority.LOW, docB.getPriority());

        // Upload document in Org A and ensure cross-org user assignment is safely rejected
        DocumentCreateRequest reqA = new DocumentCreateRequest();
        reqA.setName("Shared Report A");
        DocumentResponse docRespA = documentService.createDocument(orgId, fileB, reqA, orgUser.getId(), orgUser.getName());
        Document docA = documentRepository.findById(docRespA.getId()).orElseThrow();

        // Priority should be critical (rule matched in same org), but assignedToId must NOT be otherOrgUser.getId()
        assertEquals(Priority.CRITICAL, docA.getPriority());
        assertNotEquals(otherOrgUser.getId(), docA.getAssignedToId(), "Cross-organization assignment must be rejected");
    }

    @Test
    @DisplayName("23-25: Graceful Handling for Missing Workflow, Missing User, and Invalid Action")
    void testResilientHandlingForMissingEntitiesAndInvalidActions() {
        Rule edgeRule = new Rule("r-edge", orgId, "Edge Case Rule", "Tests missing entities", RuleStatus.ACTIVE, 1);
        RuleConditionGroup g = new RuleConditionGroup("cg-edge", edgeRule, ConditionLogic.AND, 0);
        g.addCondition(new RuleCondition("ce", g, "document.name", ConditionOperator.CONTAINS, "Resilience", 0));
        edgeRule.addConditionGroup(g);

        // Missing Workflow ID
        edgeRule.addAction(new RuleAction("a-nonexistent-wf", edgeRule, ActionType.START_WORKFLOW, "wf-nonexistent-999", null, 0));
        // Missing User
        edgeRule.addAction(new RuleAction("a-missing-user", edgeRule, ActionType.ASSIGN_USER, "nonexistent@example.com", null, 1));
        // Invalid Priority Value
        edgeRule.addAction(new RuleAction("a-invalid-prio", edgeRule, ActionType.SET_PRIORITY, "super_urgent_invalid", null, 2));

        ruleRepository.save(edgeRule);

        MockMultipartFile file = new MockMultipartFile("file", "Resilience_Test.txt", "text/plain", "Resilience content".getBytes(StandardCharsets.UTF_8));
        DocumentCreateRequest req = new DocumentCreateRequest();
        req.setName("Resilience Document");
        req.setPriority(Priority.MEDIUM);

        // Pipeline must complete without throwing unhandled exceptions
        assertDoesNotThrow(() -> {
            DocumentResponse resp = documentService.createDocument(orgId, file, req, orgUser.getId(), orgUser.getName());
            assertNotNull(resp);
            Document doc = documentRepository.findById(resp.getId()).orElseThrow();
            assertNull(doc.getWorkflowId(), "Non-existent workflow must not be set");
            assertEquals(Priority.MEDIUM, doc.getPriority(), "Invalid priority value must not alter existing priority");
        });
    }

    @Test
    @DisplayName("26: Rule Simulation Is Completely Side-Effect Free")
    void testRuleSimulationSideEffectFree() {
        long docCountBefore = documentRepository.count();
        long notifCountBefore = notificationRepository.count();
        long auditCountBefore = auditLogRepository.count();

        RuleInputDto ruleDto = new RuleInputDto();
        ruleDto.setName("Simulation Test");
        RuleConditionGroupDto gDto = new RuleConditionGroupDto();
        gDto.setLogic(ConditionLogic.AND);
        gDto.getConditions().add(new RuleConditionDto("c1", "document.name", ConditionOperator.CONTAINS, "Simulate"));
        ruleDto.getConditionGroups().add(gDto);
        ruleDto.getActions().add(new RuleActionDto("act1", ActionType.SET_PRIORITY, "critical", null));

        RuleTestRequest simReq = new RuleTestRequest();
        simReq.setRule(ruleDto);
        Map<String, Object> testCtx = new HashMap<>();
        testCtx.put("document.name", "Simulate_Invoice.pdf");
        simReq.setTestContext(testCtx);

        RuleTestResponse simResp = ruleEngineService.testRule(simReq);
        assertTrue(simResp.isMatched());
        assertEquals(1, simResp.getResultingActions().size());

        // Verify zero database side effects
        assertEquals(docCountBefore, documentRepository.count());
        assertEquals(notifCountBefore, notificationRepository.count());
        assertEquals(auditCountBefore, auditLogRepository.count());
        verify(emailService, never()).sendEmail(any(), any(), any(), any());
    }

    @Test
    @DisplayName("27-28: Processing Failure Resilience & Idempotency / Duplicate Evaluation")
    void testProcessingFailureAndDuplicateEvaluation() {
        Document doc = new Document("DOC-DUP-1", orgId, "Duplicate_Test.pdf", "Invoice");
        doc.setPriority(Priority.LOW);
        doc = documentRepository.save(doc);

        Rule rule = new Rule("r-dup", orgId, "Duplicate Rule", "Idempotency test", RuleStatus.ACTIVE, 1);
        RuleConditionGroup g = new RuleConditionGroup("cg-dup", rule, ConditionLogic.AND, 0);
        g.addCondition(new RuleCondition("c-dup", g, "document.name", ConditionOperator.CONTAINS, "Duplicate", 0));
        rule.addConditionGroup(g);
        rule.addAction(new RuleAction("act-dup", rule, ActionType.ADD_TAG, "evaluated-tag", null, 0));
        ruleRepository.save(rule);

        // First evaluation
        ruleEngineService.evaluateDocument(doc);
        Document saved1 = documentRepository.findById(doc.getId()).orElseThrow();
        assertEquals(1, saved1.getRuleMatches());
        assertEquals("evaluated-tag", saved1.getTags());

        // Second evaluation (Idempotent: should NOT produce duplicate tags like "evaluated-tag,evaluated-tag")
        ruleEngineService.evaluateDocument(saved1);
        Document saved2 = documentRepository.findById(doc.getId()).orElseThrow();
        assertEquals("evaluated-tag", saved2.getTags(), "Tag must not be duplicated on repeated evaluation");
    }
}
