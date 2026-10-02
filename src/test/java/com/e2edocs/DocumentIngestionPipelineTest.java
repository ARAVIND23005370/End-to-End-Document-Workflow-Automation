package com.e2edocs;

import com.e2edocs.dto.DocumentCreateRequest;
import com.e2edocs.dto.DocumentResponse;
import com.e2edocs.entity.Document;
import com.e2edocs.entity.Rule;
import com.e2edocs.entity.RuleAction;
import com.e2edocs.entity.RuleCondition;
import com.e2edocs.entity.RuleConditionGroup;
import com.e2edocs.entity.enums.*;
import com.e2edocs.repository.DocumentRepository;
import com.e2edocs.repository.RuleRepository;
import com.e2edocs.service.DocumentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DocumentIngestionPipelineTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Test
    @DisplayName("End-to-End Ingestion Pipeline: Upload -> Validate -> Store -> Extract -> Evaluate Rule Engine")
    void testEndToEndIngestionPipeline() {
        String orgId = "org-test-" + UUID.randomUUID();

        // 1. Configure a generic rule: If document content contains "CONFIDENTIAL" -> Set Priority URGENT & Add Tag 'restricted'
        Rule rule = new Rule("r-ingest-1", orgId, "Confidential Document Triage", "Auto-prioritize confidential items", RuleStatus.ACTIVE, 1);
        RuleConditionGroup group = new RuleConditionGroup("cg-1", rule, ConditionLogic.AND, 0);
        RuleCondition condition = new RuleCondition("c-1", group, "document.content", ConditionOperator.CONTAINS, "CONFIDENTIAL", 0);
        group.addCondition(condition);
        rule.addConditionGroup(group);

        RuleAction action1 = new RuleAction("act-1", rule, ActionType.SET_PRIORITY, "critical", null, 0);
        RuleAction action2 = new RuleAction("act-2", rule, ActionType.ADD_TAG, "restricted", null, 1);
        rule.addAction(action1);
        rule.addAction(action2);
        ruleRepository.save(rule);

        // 2. Upload a text document containing "CONFIDENTIAL"
        String docText = "SUBJECT: PROJECT ANTIGRAVITY FINANCIAL AUDIT\nSTATUS: STRICTLY CONFIDENTIAL\nTOTAL BUDGET: $500,000";
        MockMultipartFile file = new MockMultipartFile(
                "file", "audit_report_2026.txt", "text/plain", docText.getBytes(StandardCharsets.UTF_8));

        DocumentCreateRequest request = new DocumentCreateRequest();
        request.setName("Audit Report 2026");
        request.setType("Financial Audit");
        request.setSource(DocumentSource.EMAIL);
        request.setOriginalSenderEmail("cfo@enterprise.com");
        request.setOriginalSenderName("Chief Financial Officer");

        // 3. Execute ingestion
        DocumentResponse response = documentService.createDocument(
                orgId, file, request, "user-001", "Admin User");

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Audit Report 2026", response.getName());

        // 4. Verify Document Entity in Database
        Document savedDoc = documentRepository.findById(response.getId()).orElseThrow();
        assertNotNull(savedDoc.getExtractedText());
        assertTrue(savedDoc.getExtractedText().contains("STRICTLY CONFIDENTIAL"));
        assertNotNull(savedDoc.getContentPreview());
        assertEquals("audit_report_2026.txt", savedDoc.getFileName());
        assertEquals(".txt", savedDoc.getFileExtension());
        assertNotNull(savedDoc.getStoragePath());

        // 5. Verify Rule Engine Triggered Actions
        assertEquals(Priority.CRITICAL, savedDoc.getPriority(), "Rule Engine should have elevated priority to CRITICAL");
        assertNotNull(savedDoc.getTags());
        assertTrue(savedDoc.getTags().contains("restricted"), "Rule Engine should have added 'restricted' tag");
        assertEquals(1, savedDoc.getRuleMatches(), "Rule matches counter should be incremented to 1");
    }
}
