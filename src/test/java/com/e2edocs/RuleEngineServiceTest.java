package com.e2edocs;

import com.e2edocs.dto.RuleActionDto;
import com.e2edocs.dto.RuleConditionDto;
import com.e2edocs.dto.RuleConditionGroupDto;
import com.e2edocs.dto.RuleInputDto;
import com.e2edocs.dto.RuleTestRequest;
import com.e2edocs.dto.RuleTestResponse;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
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
    @DisplayName("Simulate rule test without side effects")
    void testRuleSimulation() {
        RuleInputDto ruleInput = new RuleInputDto();
        ruleInput.setName("Test Match Rule");

        RuleConditionGroupDto group = new RuleConditionGroupDto();
        group.setLogic(ConditionLogic.AND);
        group.getConditions().add(new RuleConditionDto("c1", "document.name", ConditionOperator.CONTAINS, "Contract"));
        ruleInput.getConditionGroups().add(group);

        ruleInput.getActions().add(new RuleActionDto("a1", ActionType.SET_PRIORITY, "critical", null));

        RuleTestRequest req = new RuleTestRequest();
        req.setRule(ruleInput);

        Map<String, Object> testCtx = new HashMap<>();
        testCtx.put("document.name", "Master_Contract_Agreement.pdf");
        req.setTestContext(testCtx);

        RuleTestResponse resp = ruleEngineService.testRule(req);

        assertTrue(resp.isMatched());
        assertEquals(1, resp.getResultingActions().size());
        assertEquals(ActionType.SET_PRIORITY, resp.getResultingActions().get(0).getType());

        // Verify no real repository modifications or emails sent
        verifyNoInteractions(documentRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Evaluate document updates priority and increments rule match count")
    void testEvaluateDocumentExecution() {
        Document doc = new Document("doc-1", "org-1", "Security_Report.pdf", "Audit Report");
        doc.setPriority(Priority.LOW);

        Rule rule = new Rule("r-1", "org-1", "Auto Prioritize", "Sets high priority", RuleStatus.ACTIVE, 1);
        RuleConditionGroup group = new RuleConditionGroup("cg-1", rule, ConditionLogic.AND, 0);
        group.addCondition(new RuleCondition("c-1", group, "document.name", ConditionOperator.CONTAINS, "Security", 0));
        rule.addConditionGroup(group);
        rule.addAction(new RuleAction("act-1", rule, ActionType.SET_PRIORITY, "critical", null, 0));

        when(ruleRepository.findByOrganizationIdAndStatusOrderByEvaluationOrderAsc("org-1", RuleStatus.ACTIVE))
                .thenReturn(Collections.singletonList(rule));

        ruleEngineService.evaluateDocument(doc);

        assertEquals(Priority.CRITICAL, doc.getPriority());
        assertEquals(1, doc.getRuleMatches());
        assertEquals(1, rule.getMatchCount());
        verify(documentRepository, times(1)).save(doc);
        verify(ruleRepository, times(1)).save(rule);
    }
}
