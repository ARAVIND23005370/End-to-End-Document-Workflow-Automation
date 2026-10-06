package com.e2edocs.config;

import com.e2edocs.dto.RuleActionDto;
import com.e2edocs.dto.RuleConditionDto;
import com.e2edocs.dto.RuleConditionGroupDto;
import com.e2edocs.dto.RuleInputDto;
import com.e2edocs.entity.*;
import com.e2edocs.entity.enums.*;
import com.e2edocs.repository.*;
import com.e2edocs.service.RuleEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final DocumentCategoryRepository documentCategoryRepository;
    private final DocumentRepository documentRepository;
    private final RuleRepository ruleRepository;
    private final RuleEngineService ruleEngineService;
    private final WorkflowRepository workflowRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${e2edocs.seed.enabled:false}")
    private boolean seedEnabled;

    @Value("${e2edocs.seed.admin-email:admin@e2edocs.local}")
    private String adminEmail;

    @Value("${e2edocs.seed.admin-password:admin123}")
    private String adminPassword;

    public DataSeeder(
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            TeamRepository teamRepository,
            DocumentCategoryRepository documentCategoryRepository,
            DocumentRepository documentRepository,
            RuleRepository ruleRepository,
            RuleEngineService ruleEngineService,
            WorkflowRepository workflowRepository,
            NotificationRepository notificationRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.teamRepository = teamRepository;
        this.documentCategoryRepository = documentCategoryRepository;
        this.documentRepository = documentRepository;
        this.ruleRepository = ruleRepository;
        this.ruleEngineService = ruleEngineService;
        this.workflowRepository = workflowRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            logger.info("Database seeding is disabled (e2edocs.seed.enabled=false).");
            return;
        }

        if (organizationRepository.count() > 0) {
            logger.info("Database already seeded with organization data. Skipping seed.");
            return;
        }

        logger.info("Seeding development data for E2EDocs platform...");

        // 1. Organization
        String orgId = "org-001";
        Organization org = new Organization(orgId, "Acme Global Enterprises", "ACME", "Enterprise document processing workspace", "acme.example.com");
        organizationRepository.save(org);

        // 2. Departments
        Department dept1 = new Department("dept-001", orgId, "Operations", "OPS", "General operations and document logistics");
        Department dept2 = new Department("dept-002", orgId, "Finance & Accounts", "FIN", "Financial statements, invoices, and payment audits");
        Department dept3 = new Department("dept-003", orgId, "Legal & Contracts", "LEG", "Contract reviews, compliance, and regulatory agreements");
        Department dept4 = new Department("dept-004", orgId, "Human Resources", "HR", "Personnel files, onboarding documents, and employee records");
        Department dept5 = new Department("dept-005", orgId, "Risk & Compliance", "RISK", "Risk audits, safety certificates, and security compliance");
        Department dept6 = new Department("dept-006", orgId, "Engineering & Product", "ENG", "Technical specifications, design docs, and system architecture");
        departmentRepository.saveAll(Arrays.asList(dept1, dept2, dept3, dept4, dept5, dept6));

        // 3. Teams
        Team team1 = new Team("team-001", orgId, dept1.getId(), "Document Intake Team", "u-002");
        Team team2 = new Team("team-002", orgId, dept3.getId(), "Contract Review Team", "u-004");
        teamRepository.saveAll(Arrays.asList(team1, team2));

        // 4. Document Categories
        DocumentCategory cat1 = new DocumentCategory("cat-001", orgId, "Service Agreement", "Contracts and service level agreements");
        DocumentCategory cat2 = new DocumentCategory("cat-002", orgId, "Invoice", "Commercial billing invoices and credit notes");
        DocumentCategory cat3 = new DocumentCategory("cat-003", orgId, "Purchase Order", "Procurement orders and vendor confirmations");
        DocumentCategory cat4 = new DocumentCategory("cat-004", orgId, "Identity Document", "Passports, ID cards, and official identification");
        DocumentCategory cat5 = new DocumentCategory("cat-005", orgId, "Tax Form", "Tax returns, W-9, and withholding schedules");
        DocumentCategory cat6 = new DocumentCategory("cat-006", orgId, "Audit Report", "Annual audits, security reviews, and compliance certifications");
        documentCategoryRepository.saveAll(Arrays.asList(cat1, cat2, cat3, cat4, cat5, cat6));

        // 5. Users
        User adminUser = new User("u-001", orgId, "System Administrator", adminEmail, passwordEncoder.encode(adminPassword), UserRole.SUPER_ADMIN);
        adminUser.setDepartment("Engineering & Product");
        adminUser.setDepartmentId("dept-006");

        User user2 = new User("u-002", orgId, "Alex Vance", "alex.vance@example.com", passwordEncoder.encode("password123"), UserRole.ADMIN);
        user2.setDepartment("Operations");
        user2.setDepartmentId("dept-001");

        User user3 = new User("u-003", orgId, "Marcus Brody", "marcus.brody@example.com", passwordEncoder.encode("password123"), UserRole.MANAGER);
        user3.setDepartment("Finance & Accounts");
        user3.setDepartmentId("dept-002");

        User user4 = new User("u-004", orgId, "James Okafor", "james.okafor@example.com", passwordEncoder.encode("password123"), UserRole.REVIEWER);
        user4.setDepartment("Legal & Contracts");
        user4.setDepartmentId("dept-003");

        User user5 = new User("u-005", orgId, "Elena Rostova", "elena.rostova@example.com", passwordEncoder.encode("password123"), UserRole.USER);
        user5.setDepartment("Risk & Compliance");
        user5.setDepartmentId("dept-005");

        userRepository.saveAll(Arrays.asList(adminUser, user2, user3, user4, user5));

        // 6. Section-Isolated Starter Rules
        // 6a. Decision Rule (Verifies Tax ID / Verification Code -> APPROVE)
        RuleInputDto decisionRule = new RuleInputDto();
        decisionRule.setName("Verified Document Approval Rule");
        decisionRule.setDescription("Auto-approves documents containing verified Tax ID or verification code.");
        decisionRule.setStatus(RuleStatus.ACTIVE);
        decisionRule.setRuleType(RuleType.DECISION);
        decisionRule.setEvaluationOrder(1);
        RuleConditionGroupDto condGroup1 = new RuleConditionGroupDto();
        condGroup1.setLogic(ConditionLogic.OR);
        condGroup1.getConditions().add(new RuleConditionDto("c-001", "extracted.text", ConditionOperator.CONTAINS, "Tax ID"));
        condGroup1.getConditions().add(new RuleConditionDto("c-002", "extracted.text", ConditionOperator.CONTAINS, "VERIFIED"));
        decisionRule.getConditionGroups().add(condGroup1);
        decisionRule.getActions().add(new RuleActionDto("act-001", ActionType.SET_DECISION, "approved", null));
        ruleEngineService.createRule(orgId, decisionRule, "System Administrator");

        // 6b. Folder Rule (Auto-classify Invoices and Contracts)
        RuleInputDto folderRule = new RuleInputDto();
        folderRule.setName("Auto-Classify Commercial Invoices");
        folderRule.setDescription("Classifies billing documents into the Invoices virtual category.");
        folderRule.setStatus(RuleStatus.ACTIVE);
        folderRule.setRuleType(RuleType.FOLDER);
        folderRule.setEvaluationOrder(1);
        RuleConditionGroupDto condGroupFolder = new RuleConditionGroupDto();
        condGroupFolder.setLogic(ConditionLogic.OR);
        condGroupFolder.getConditions().add(new RuleConditionDto("c-f01", "extracted.text", ConditionOperator.CONTAINS, "INVOICE"));
        folderRule.getConditionGroups().add(condGroupFolder);
        folderRule.getActions().add(new RuleActionDto("act-f01", ActionType.ASSIGN_FOLDER, "Invoices", null));
        ruleEngineService.createRule(orgId, folderRule, "System Administrator");

        // 6c. Sorting Rule (Escalate Urgent Documents)
        RuleInputDto sortingRule = new RuleInputDto();
        sortingRule.setName("High Priority Urgent Sorting");
        sortingRule.setDescription("Sets Critical sorting priority for urgent keywords.");
        sortingRule.setStatus(RuleStatus.ACTIVE);
        sortingRule.setRuleType(RuleType.SORTING);
        sortingRule.setEvaluationOrder(1);
        RuleConditionGroupDto condGroup3 = new RuleConditionGroupDto();
        condGroup3.setLogic(ConditionLogic.OR);
        condGroup3.getConditions().add(new RuleConditionDto("c-004", "extracted.text", ConditionOperator.CONTAINS, "Urgent"));
        condGroup3.getConditions().add(new RuleConditionDto("c-005", "extracted.text", ConditionOperator.CONTAINS, "URGENT"));
        sortingRule.getConditionGroups().add(condGroup3);
        sortingRule.getActions().add(new RuleActionDto("act-003", ActionType.SET_PRIORITY, "critical", null));
        ruleEngineService.createRule(orgId, sortingRule, "System Administrator");

        // 6d. Routing Rule (Route to Specialist / Operations)
        RuleInputDto routingRule = new RuleInputDto();
        routingRule.setName("Accounts Billing Assignment");
        routingRule.setDescription("Routes invoice documents to Accounts team.");
        routingRule.setStatus(RuleStatus.ACTIVE);
        routingRule.setRuleType(RuleType.ROUTING);
        routingRule.setEvaluationOrder(1);
        RuleConditionGroupDto condGroup2 = new RuleConditionGroupDto();
        condGroup2.setLogic(ConditionLogic.AND);
        condGroup2.getConditions().add(new RuleConditionDto("c-003", "extracted.text", ConditionOperator.CONTAINS, "INVOICE"));
        routingRule.getConditionGroups().add(condGroup2);
        routingRule.getActions().add(new RuleActionDto("act-002", ActionType.ASSIGN_DEPARTMENT, "Finance & Accounts", null));
        ruleEngineService.createRule(orgId, routingRule, "System Administrator");

        // 7. Workflows
        Workflow wf = new Workflow("wf-001", orgId, "Standard Document Intake",
                "Generic automated pipeline for classifying and routing incoming documents.", WorkflowStatus.ACTIVE, "System Administrator");
        wf.addStep(new WorkflowStep("ws-0", wf, "Ingestion", "trigger", "completed", 1));
        wf.addStep(new WorkflowStep("ws-1", wf, "Rule Evaluation", "evaluation", "completed", 2));
        wf.addStep(new WorkflowStep("ws-2", wf, "Department Routing", "routing", "completed", 3));
        wf.addStep(new WorkflowStep("ws-3", wf, "Assignment", "action", "completed", 4));
        wf.addStep(new WorkflowStep("ws-4", wf, "Notification", "notification", "completed", 5));
        wf.setDocumentsProcessed(342);
        workflowRepository.save(wf);

        // 8. Documents
        Document doc1 = new Document("DOC-2026-0847", orgId, "Contract_2026_014.pdf", "Service Agreement");
        doc1.setDescription("Service agreement requiring review.");
        doc1.setStatus(DocumentStatus.REVIEW);
        doc1.setPriority(Priority.HIGH);
        doc1.setSource(DocumentSource.EMAIL);
        doc1.setOriginalSenderEmail("contracts@example.org");
        doc1.setOriginalSenderName("Alex Vance");
        doc1.setDepartment("Legal & Contracts");
        doc1.setDepartmentId("dept-003");
        doc1.setAssignedTo("James Okafor");
        doc1.setAssignedToId("u-004");
        doc1.setSize(2457600L);
        doc1.setTags("contract,service-agreement,external");
        doc1.setRuleMatches(3);
        doc1.setWorkflowId("wf-001");
        doc1.setContentPreview("This Master Services Agreement (\"Agreement\") is entered into between Acme Enterprises and Partner Corp...");
        doc1.setCreatedAt(Instant.now().minus(4, ChronoUnit.DAYS));

        Document doc2 = new Document("DOC-2026-0848", orgId, "Vendor_Invoice_INV-9921.pdf", "Invoice");
        doc2.setDescription("Vendor monthly recurring invoice.");
        doc2.setStatus(DocumentStatus.APPROVED);
        doc2.setPriority(Priority.MEDIUM);
        doc2.setSource(DocumentSource.MANUAL_UPLOAD);
        doc2.setDepartment("Finance & Accounts");
        doc2.setDepartmentId("dept-002");
        doc2.setAssignedTo("Marcus Brody");
        doc2.setAssignedToId("u-003");
        doc2.setSize(1048576L);
        doc2.setTags("invoice,vendor,finance");
        doc2.setCreatedAt(Instant.now().minus(2, ChronoUnit.DAYS));

        Document doc3 = new Document("DOC-2026-0849", orgId, "Internal_Policy_Update.pdf", "Audit Report");
        doc3.setDescription("Annual internal compliance update.");
        doc3.setStatus(DocumentStatus.PROCESSING);
        doc3.setPriority(Priority.LOW);
        doc3.setSource(DocumentSource.API);
        doc3.setDepartment("Operations");
        doc3.setDepartmentId("dept-001");
        doc3.setSize(524288L);
        doc3.setTags("policy,internal");
        doc3.setCreatedAt(Instant.now().minus(1, ChronoUnit.DAYS));

        Document doc4 = new Document("DOC-2026-0850", orgId, "Security_Audit_Report_2026.pdf", "Audit Report");
        doc4.setDescription("Critical security infrastructure audit.");
        doc4.setStatus(DocumentStatus.REVIEW);
        doc4.setPriority(Priority.CRITICAL);
        doc4.setSource(DocumentSource.INTEGRATION);
        doc4.setDepartment("Risk & Compliance");
        doc4.setDepartmentId("dept-005");
        doc4.setAssignedTo("Elena Rostova");
        doc4.setAssignedToId("u-005");
        doc4.setSize(4194304L);
        doc4.setTags("security,audit,critical");
        doc4.setCreatedAt(Instant.now().minus(6, ChronoUnit.HOURS));

        documentRepository.saveAll(Arrays.asList(doc1, doc2, doc3, doc4));

        // 9. Notifications
        Notification notif1 = new Notification("n-010", orgId, "u-001", "Document requires review",
                "Security_Audit_Report_2026.pdf needs your attention. Priority: Critical.", NotificationType.WARNING);
        notif1.setDocumentId("DOC-2026-0850");
        notif1.setDocumentName("Security_Audit_Report_2026.pdf");
        notif1.setPriority(Priority.CRITICAL);
        notif1.setTimestamp(Instant.now().minus(2, ChronoUnit.HOURS));
        notificationRepository.save(notif1);

        // 10. Audit Logs
        AuditLog audit1 = new AuditLog("au-001", orgId, "u-001", "System Administrator", AuditAction.UPLOADED,
                "Document", "DOC-2026-0849", AuditStatus.SUCCESS, "Uploaded document: Internal_Policy_Update.pdf", "192.168.1.10");
        AuditLog audit2 = new AuditLog("au-002", orgId, "u-004", "James Okafor", AuditAction.REVIEWED,
                "Document", "DOC-2026-0847", AuditStatus.SUCCESS, "Initial legal review completed", "192.168.1.15");
        auditLogRepository.saveAll(Arrays.asList(audit1, audit2));

        logger.info("E2EDocs development seed data successfully initialized.");
    }
}
