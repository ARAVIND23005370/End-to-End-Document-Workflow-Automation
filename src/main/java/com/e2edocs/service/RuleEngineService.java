package com.e2edocs.service;

import com.e2edocs.dto.*;
import com.e2edocs.entity.*;
import com.e2edocs.entity.enums.*;
import com.e2edocs.exception.BadRequestException;
import com.e2edocs.exception.ResourceNotFoundException;
import com.e2edocs.repository.DocumentRepository;
import com.e2edocs.repository.RuleRepository;
import com.e2edocs.repository.UserRepository;
import com.e2edocs.repository.WorkflowRepository;
import com.e2edocs.service.email.EmailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RuleEngineService {

    private static final Logger logger = LoggerFactory.getLogger(RuleEngineService.class);

    private final RuleRepository ruleRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final WorkflowRepository workflowRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public RuleEngineService(
            RuleRepository ruleRepository,
            DocumentRepository documentRepository,
            UserRepository userRepository,
            WorkflowRepository workflowRepository,
            EmailService emailService,
            NotificationService notificationService,
            AuditService auditService) {
        this.ruleRepository = ruleRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.workflowRepository = workflowRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional(readOnly = true)
    public List<RuleResponse> getAllRules(String organizationId) {
        return ruleRepository.findByOrganizationIdOrderByEvaluationOrderAsc(organizationId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RuleResponse getRuleById(String id) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found: " + id));
        return mapToResponse(rule);
    }

    @Transactional
    public RuleResponse createRule(String organizationId, RuleInputDto input, String createdBy) {
        String ruleId = "r-" + UUID.randomUUID().toString().substring(0, 8);
        Rule rule = new Rule(
                ruleId,
                organizationId,
                input.getName(),
                input.getDescription(),
                input.getStatus() != null ? input.getStatus() : RuleStatus.ACTIVE,
                input.getEvaluationOrder() != null ? input.getEvaluationOrder() : 1
        );
        rule.setCreatedBy(createdBy);

        populateConditionGroupsAndActions(rule, input);

        Rule saved = ruleRepository.save(rule);

        auditService.log(organizationId, "SYSTEM", createdBy, AuditAction.CREATED,
                "Rule", saved.getId(), AuditStatus.SUCCESS, "Created rule: " + saved.getName(), "SYSTEM");

        return mapToResponse(saved);
    }

    @Transactional
    public RuleResponse updateRule(String id, RuleInputDto input, String updatedBy) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found: " + id));

        rule.setName(input.getName());
        rule.setDescription(input.getDescription());
        if (input.getStatus() != null) {
            rule.setStatus(input.getStatus());
        }
        if (input.getEvaluationOrder() != null) {
            rule.setEvaluationOrder(input.getEvaluationOrder());
        }

        rule.getConditionGroups().clear();
        rule.getActions().clear();

        populateConditionGroupsAndActions(rule, input);

        Rule saved = ruleRepository.save(rule);

        auditService.log(saved.getOrganizationId(), "SYSTEM", updatedBy, AuditAction.UPDATED,
                "Rule", saved.getId(), AuditStatus.SUCCESS, "Updated rule: " + saved.getName(), "SYSTEM");

        return mapToResponse(saved);
    }

    @Transactional
    public void deleteRule(String id, String deletedBy) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found: " + id));
        String orgId = rule.getOrganizationId();
        String name = rule.getName();

        ruleRepository.delete(rule);

        auditService.log(orgId, "SYSTEM", deletedBy, AuditAction.DELETED,
                "Rule", id, AuditStatus.SUCCESS, "Deleted rule: " + name, "SYSTEM");
    }

    @Transactional
    public void evaluateDocument(Document document) {
        List<Rule> activeRules = ruleRepository.findByOrganizationIdAndStatusOrderByEvaluationOrderAsc(
                document.getOrganizationId(), RuleStatus.ACTIVE);

        int totalMatches = 0;

        for (Rule rule : activeRules) {
            boolean matched = evaluateRuleOnDocument(rule, document);
            if (matched) {
                totalMatches++;
                rule.setMatchCount(rule.getMatchCount() != null ? rule.getMatchCount() + 1 : 1);
                rule.setLastTriggered(Instant.now());
                ruleRepository.save(rule);

                executeActions(rule, document);

                auditService.log(document.getOrganizationId(), "SYSTEM", "Rule Engine", AuditAction.ROUTED,
                        "Rule", rule.getId(), AuditStatus.SUCCESS,
                        "Rule '" + rule.getName() + "' matched on document " + document.getName(), "SYSTEM");
            }
        }

        document.setRuleMatches(totalMatches);
        documentRepository.save(document);
    }

    public RuleTestResponse testRule(RuleTestRequest request) {
        RuleTestResponse response = new RuleTestResponse();
        if (request.getRule() == null) {
            throw new BadRequestException("Rule configuration is required for testing");
        }

        Map<String, Object> context = buildTestContext(request.getDocument(), request.getTestContext());
        List<String> matchedConds = new ArrayList<>();
        List<String> unmatchedConds = new ArrayList<>();
        List<String> logs = new ArrayList<>();

        boolean ruleMatches = false;
        List<RuleConditionGroupDto> groups = request.getRule().getConditionGroups();

        if (groups == null || groups.isEmpty()) {
            ruleMatches = true;
            logs.add("Rule has no conditions, defaults to match.");
        } else {
            for (RuleConditionGroupDto group : groups) {
                boolean groupResult = evaluateConditionGroupDto(group, context, matchedConds, unmatchedConds, logs);
                if (groupResult) {
                    ruleMatches = true;
                }
            }
        }

        response.setMatched(ruleMatches);
        response.setMatchedConditions(matchedConds);
        response.setUnmatchedConditions(unmatchedConds);
        response.setLogs(logs);

        if (ruleMatches && request.getRule().getActions() != null) {
            response.setResultingActions(request.getRule().getActions());
        }

        return response;
    }

    private boolean evaluateRuleOnDocument(Rule rule, Document document) {
        Map<String, Object> context = buildDocumentContext(document);
        List<RuleConditionGroup> groups = rule.getConditionGroups();
        if (groups == null || groups.isEmpty()) {
            return true;
        }

        for (RuleConditionGroup group : groups) {
            if (evaluateConditionGroup(group, context)) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateConditionGroup(RuleConditionGroup group, Map<String, Object> context) {
        List<RuleCondition> conditions = group.getConditions();
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }

        if (group.getLogic() == ConditionLogic.AND) {
            for (RuleCondition condition : conditions) {
                if (!evaluateCondition(condition.getField(), condition.getOperator(), condition.getValue(), context)) {
                    return false;
                }
            }
            return true;
        } else { // OR
            for (RuleCondition condition : conditions) {
                if (evaluateCondition(condition.getField(), condition.getOperator(), condition.getValue(), context)) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean evaluateConditionGroupDto(RuleConditionGroupDto group, Map<String, Object> context,
                                             List<String> matchedConds, List<String> unmatchedConds, List<String> logs) {
        List<RuleConditionDto> conditions = group.getConditions();
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }

        boolean isAnd = group.getLogic() == ConditionLogic.AND;
        boolean overall = isAnd;

        for (RuleConditionDto condition : conditions) {
            boolean condResult = evaluateCondition(condition.getField(), condition.getOperator(), condition.getValue(), context);
            String label = condition.getField() + " " + (condition.getOperator() != null ? condition.getOperator().getValue() : "") + " " + condition.getValue();

            if (condResult) {
                matchedConds.add(label);
                logs.add("Condition PASSED: " + label);
            } else {
                unmatchedConds.add(label);
                logs.add("Condition FAILED: " + label);
            }

            if (isAnd) {
                if (!condResult) overall = false;
            } else {
                if (condResult) overall = true;
            }
        }
        return overall;
    }

    public boolean evaluateCondition(String field, ConditionOperator operator, String expectedValue, Map<String, Object> context) {
        Object actualValue = resolveFieldValue(field, context);
        return compareValues(actualValue, operator, expectedValue);
    }

    private Object resolveFieldValue(String field, Map<String, Object> context) {
        if (field == null || context == null) return null;

        if (context.containsKey(field)) {
            return context.get(field);
        }

        if (field.startsWith("metadata.")) {
            String metaKey = field.substring("metadata.".length());
            Object metadataObj = context.get("metadata");
            if (metadataObj instanceof Map<?, ?> metaMap) {
                return metaMap.get(metaKey);
            }
        }

        if (field.startsWith("sender.")) {
            String senderKey = field.substring("sender.".length());
            if ("email".equalsIgnoreCase(senderKey)) return context.get("sender.email");
            if ("name".equalsIgnoreCase(senderKey)) return context.get("sender.name");
        }

        if (field.startsWith("file.")) {
            String fileKey = field.substring("file.".length());
            if ("extension".equalsIgnoreCase(fileKey)) return context.get("file.extension");
            if ("size".equalsIgnoreCase(fileKey)) return context.get("file.size");
        }

        if (field.startsWith("extracted.")) {
            String extKey = field.substring("extracted.".length());
            if ("text".equalsIgnoreCase(extKey)) return context.get("extracted.text");
            if ("category".equalsIgnoreCase(extKey)) return context.get("extracted.category");
        }

        return null;
    }

    private boolean compareValues(Object actual, ConditionOperator operator, String expected) {
        String actualStr = actual != null ? String.valueOf(actual).trim() : "";
        String expectedStr = expected != null ? expected.trim() : "";

        if (operator == null) return false;

        switch (operator) {
            case EQUALS:
                return actualStr.equalsIgnoreCase(expectedStr);
            case NOT_EQUALS:
                return !actualStr.equalsIgnoreCase(expectedStr);
            case CONTAINS:
                return actualStr.toLowerCase().contains(expectedStr.toLowerCase());
            case NOT_CONTAINS:
                return !actualStr.toLowerCase().contains(expectedStr.toLowerCase());
            case STARTS_WITH:
                return actualStr.toLowerCase().startsWith(expectedStr.toLowerCase());
            case ENDS_WITH:
                return actualStr.toLowerCase().endsWith(expectedStr.toLowerCase());
            case MATCHES:
                try {
                    return Pattern.compile(expected, Pattern.CASE_INSENSITIVE).matcher(actualStr).find();
                } catch (Exception e) {
                    return false;
                }
            case GREATER_THAN:
                try {
                    double actNum = Double.parseDouble(actualStr);
                    double expNum = Double.parseDouble(expectedStr);
                    return actNum > expNum;
                } catch (NumberFormatException e) {
                    return actualStr.compareToIgnoreCase(expectedStr) > 0;
                }
            case LESS_THAN:
                try {
                    double actNum = Double.parseDouble(actualStr);
                    double expNum = Double.parseDouble(expectedStr);
                    return actNum < expNum;
                } catch (NumberFormatException e) {
                    return actualStr.compareToIgnoreCase(expectedStr) < 0;
                }
            case IN:
                List<String> inValues = Arrays.stream(expectedStr.split(","))
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .toList();
                return inValues.contains(actualStr.toLowerCase());
            case NOT_IN:
                List<String> notInValues = Arrays.stream(expectedStr.split(","))
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .toList();
                return !notInValues.contains(actualStr.toLowerCase());
            default:
                return false;
        }
    }

    private void executeActions(Rule rule, Document document) {
        for (RuleAction action : rule.getActions()) {
            if (action.getType() == null) continue;

            switch (action.getType()) {
                case SET_PRIORITY:
                    try {
                        Priority priority = Priority.fromValue(action.getValue());
                        if (priority != null) {
                            document.setPriority(priority);
                            auditService.log(document.getOrganizationId(), "SYSTEM", "Rule Engine", AuditAction.UPDATED,
                                    "Document", document.getId(), AuditStatus.SUCCESS,
                                    "Set priority to " + priority.getValue() + " by rule '" + rule.getName() + "'", "SYSTEM");
                        }
                    } catch (Exception e) {
                        logger.warn("Invalid priority value in rule action: {}", action.getValue());
                    }
                    break;
                case ASSIGN_USER:
                    String userVal = action.getValue();
                    if (userVal != null && !userVal.isBlank()) {
                        Optional<User> userOpt = userRepository.findById(userVal);
                        if (userOpt.isEmpty()) {
                            userOpt = userRepository.findByEmailIgnoreCase(userVal);
                        }
                        if (userOpt.isPresent()) {
                            User targetUser = userOpt.get();
                            if (targetUser.getOrganizationId().equals(document.getOrganizationId())) {
                                document.setAssignedTo(targetUser.getName());
                                document.setAssignedToId(targetUser.getId());
                                auditService.log(document.getOrganizationId(), "SYSTEM", "Rule Engine", AuditAction.ASSIGNED,
                                        "Document", document.getId(), AuditStatus.SUCCESS,
                                        "Assigned document to " + targetUser.getName() + " by rule '" + rule.getName() + "'", "SYSTEM");
                            } else {
                                logger.warn("Cross-organization assignment attempt blocked for user {} in org {} on doc org {}",
                                        targetUser.getId(), targetUser.getOrganizationId(), document.getOrganizationId());
                            }
                        } else {
                            document.setAssignedTo(userVal);
                            auditService.log(document.getOrganizationId(), "SYSTEM", "Rule Engine", AuditAction.ASSIGNED,
                                    "Document", document.getId(), AuditStatus.SUCCESS,
                                    "Assigned document to " + userVal + " by rule '" + rule.getName() + "'", "SYSTEM");
                        }
                    }
                    break;
                case START_WORKFLOW:
                    String wfVal = action.getValue();
                    if (wfVal != null && !wfVal.isBlank()) {
                        Optional<Workflow> wfOpt = workflowRepository.findById(wfVal);
                        if (wfOpt.isEmpty()) {
                            wfOpt = workflowRepository.findByOrganizationId(document.getOrganizationId()).stream()
                                    .filter(w -> w.getName().equalsIgnoreCase(wfVal))
                                    .findFirst();
                        }
                        if (wfOpt.isPresent()) {
                            Workflow wf = wfOpt.get();
                            if (wf.getOrganizationId().equals(document.getOrganizationId()) && wf.getStatus() == WorkflowStatus.ACTIVE) {
                                document.setWorkflowId(wf.getId());
                                wf.setDocumentsProcessed((wf.getDocumentsProcessed() != null ? wf.getDocumentsProcessed() : 0) + 1);
                                workflowRepository.save(wf);
                                auditService.log(document.getOrganizationId(), "SYSTEM", "Rule Engine", AuditAction.UPDATED,
                                        "Workflow", wf.getId(), AuditStatus.SUCCESS,
                                        "Started workflow '" + wf.getName() + "' for document " + document.getName(), "SYSTEM");
                            } else {
                                logger.warn("Workflow {} is inactive or in different organization", wfVal);
                            }
                        } else {
                            logger.warn("Workflow not found for action value: {}", wfVal);
                        }
                    }
                    break;
                case SET_DECISION:
                    try {
                        DocumentStatus status = DocumentStatus.fromValue(action.getValue());
                        if (status != null) {
                            document.setStatus(status);
                            AuditAction auditAct = status == DocumentStatus.APPROVED ? AuditAction.APPROVED :
                                    (status == DocumentStatus.REJECTED ? AuditAction.REJECTED : AuditAction.UPDATED);
                            auditService.log(document.getOrganizationId(), "SYSTEM", "Rule Engine", auditAct,
                                    "Document", document.getId(), AuditStatus.SUCCESS,
                                    "Set decision/status to " + status.getValue() + " by rule '" + rule.getName() + "'", "SYSTEM");
                        }
                    } catch (Exception e) {
                        logger.warn("Invalid decision/status value in rule action: {}", action.getValue());
                    }
                    break;
                case ADD_TAG:
                    if (action.getValue() != null && !action.getValue().isBlank()) {
                        String tag = action.getValue().trim();
                        String currentTags = document.getTags() != null ? document.getTags() : "";
                        List<String> tagList = Arrays.stream(currentTags.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());
                        if (!tagList.contains(tag)) {
                            tagList.add(tag);
                            document.setTags(String.join(",", tagList));
                            auditService.log(document.getOrganizationId(), "SYSTEM", "Rule Engine", AuditAction.UPDATED,
                                    "Document", document.getId(), AuditStatus.SUCCESS,
                                    "Added tag '" + tag + "' by rule '" + rule.getName() + "'", "SYSTEM");
                        }
                    }
                    break;
                case SEND_NOTIFICATION:
                    notificationService.createNotification(
                            document.getOrganizationId(),
                            document.getAssignedToId(),
                            "Rule Action: " + rule.getName(),
                            action.getValue() != null ? action.getValue() : "Action applied to document " + document.getName(),
                            NotificationType.INFO,
                            document.getId(),
                            document.getName(),
                            document.getPriority()
                    );
                    auditService.log(document.getOrganizationId(), "SYSTEM", "Rule Engine", AuditAction.CREATED,
                            "Notification", document.getId(), AuditStatus.SUCCESS,
                            "Dispatched notification for rule '" + rule.getName() + "'", "SYSTEM");
                    break;
                case SEND_EMAIL:
                    handleSendEmailAction(action, document, rule);
                    break;
                case FORWARD_DOCUMENT:
                    logger.info("Forwarding document {} to {}", document.getId(), action.getValue());
                    auditService.log(document.getOrganizationId(), "SYSTEM", "Rule Engine", AuditAction.ROUTED,
                            "Document", document.getId(), AuditStatus.SUCCESS,
                            "Forwarded document to: " + action.getValue() + " by rule '" + rule.getName() + "'", "SYSTEM");
                    break;
            }
        }
    }

    private void handleSendEmailAction(RuleAction action, Document document, Rule rule) {
        if (action.getEmailConfigJson() == null || action.getEmailConfigJson().isBlank()) return;
        try {
            EmailConfigDto emailConfig = objectMapper.readValue(action.getEmailConfigJson(), EmailConfigDto.class);
            String recipient = resolveRecipient(emailConfig, document);
            if (recipient != null && !recipient.isBlank()) {
                Map<String, Object> ctx = buildDocumentContext(document);
                ctx.put("document.id", document.getId());
                ctx.put("document.name", document.getName());
                ctx.put("document.type", document.getType());
                ctx.put("document.status", document.getStatus() != null ? document.getStatus().getValue() : "");
                ctx.put("document.priority", document.getPriority() != null ? document.getPriority().getValue() : "");

                emailService.sendEmail(recipient, emailConfig.getSubject(), emailConfig.getMessage(), ctx);

                auditService.log(document.getOrganizationId(), "SYSTEM", "Rule Engine", AuditAction.CREATED,
                        "Email", document.getId(), AuditStatus.SUCCESS,
                        "Sent email to " + recipient + " by rule '" + rule.getName() + "'", "SYSTEM");
            }
        } catch (JsonProcessingException e) {
            logger.warn("Failed to parse EmailConfigJson for rule action: {}", e.getMessage());
        }
    }

    private String resolveRecipient(EmailConfigDto config, Document doc) {
        if (config == null || config.getRecipientType() == null) return null;
        switch (config.getRecipientType()) {
            case ORIGINAL_SENDER:
                return doc.getOriginalSenderEmail();
            case ASSIGNED_USER:
                if (doc.getAssignedToId() != null) {
                    return userRepository.findById(doc.getAssignedToId()).map(User::getEmail).orElse(null);
                }
                return null;
            case CUSTOM:
                return config.getCustomRecipient();
            case DEPARTMENT:
                return doc.getDepartment() != null ? doc.getDepartment().toLowerCase().replace(" ", "") + "@example.com" : null;
            default:
                return null;
        }
    }

    private Map<String, Object> buildDocumentContext(Document doc) {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("document.name", doc.getName());
        ctx.put("document.type", doc.getType());
        ctx.put("document.description", doc.getDescription());
        ctx.put("document.content", doc.getExtractedText() != null ? doc.getExtractedText() : doc.getContentPreview());
        ctx.put("sender.email", doc.getOriginalSenderEmail());
        ctx.put("sender.name", doc.getOriginalSenderName());
        ctx.put("file.extension", doc.getFileExtension());
        ctx.put("file.size", doc.getSize());
        ctx.put("document.priority", doc.getPriority() != null ? doc.getPriority().getValue() : "");
        ctx.put("document.department", doc.getDepartment());
        ctx.put("document.tags", doc.getTags());
        ctx.put("extracted.text", doc.getExtractedText());
        ctx.put("extracted.category", doc.getType());

        if (doc.getMetadataJson() != null && !doc.getMetadataJson().isBlank()) {
            try {
                Map<String, Object> meta = objectMapper.readValue(doc.getMetadataJson(), new TypeReference<Map<String, Object>>() {});
                ctx.put("metadata", meta);
                if (meta.containsKey("source")) ctx.put("metadata.source", meta.get("source"));
                if (meta.containsKey("subject")) ctx.put("metadata.subject", meta.get("subject"));
                if (meta.containsKey("recipient_email")) ctx.put("metadata.recipient_email", meta.get("recipient_email"));
            } catch (Exception ignored) {
            }
        }
        if (doc.getSource() != null) {
            ctx.put("metadata.source", doc.getSource().getValue());
        }
        return ctx;
    }

    private Map<String, Object> buildTestContext(DocumentResponse doc, Map<String, Object> testContext) {
        Map<String, Object> ctx = new HashMap<>();
        if (doc != null) {
            ctx.put("document.name", doc.getName());
            ctx.put("document.type", doc.getType());
            ctx.put("document.description", doc.getDescription());
            if (doc.getOriginalSender() != null) {
                ctx.put("sender.email", doc.getOriginalSender().getEmail());
                ctx.put("sender.name", doc.getOriginalSender().getName());
            }
            if (doc.getSource() != null) {
                ctx.put("metadata.source", doc.getSource().getValue());
            }
            if (doc.getPriority() != null) {
                ctx.put("document.priority", doc.getPriority().getValue());
            }
            ctx.put("document.department", doc.getDepartment());
            ctx.put("document.tags", doc.getTags() != null ? String.join(",", doc.getTags()) : "");
            if (doc.getMetadata() != null) {
                ctx.put("metadata", doc.getMetadata());
                if (doc.getMetadata().containsKey("source")) ctx.put("metadata.source", doc.getMetadata().get("source"));
                if (doc.getMetadata().containsKey("subject")) ctx.put("metadata.subject", doc.getMetadata().get("subject"));
                if (doc.getMetadata().containsKey("recipient_email")) ctx.put("metadata.recipient_email", doc.getMetadata().get("recipient_email"));
            }
        }
        if (testContext != null) {
            ctx.putAll(testContext);
        }
        return ctx;
    }

    private void populateConditionGroupsAndActions(Rule rule, RuleInputDto input) {
        if (input.getConditionGroups() != null) {
            int gIdx = 0;
            for (RuleConditionGroupDto gDto : input.getConditionGroups()) {
                String gId = gDto.getId() != null ? gDto.getId() : "cg-" + UUID.randomUUID().toString().substring(0, 8);
                RuleConditionGroup group = new RuleConditionGroup(gId, rule, gDto.getLogic(), gIdx++);

                if (gDto.getConditions() != null) {
                    int cIdx = 0;
                    for (RuleConditionDto cDto : gDto.getConditions()) {
                        String cId = cDto.getId() != null ? cDto.getId() : "c-" + UUID.randomUUID().toString().substring(0, 8);
                        RuleCondition condition = new RuleCondition(cId, group, cDto.getField(), cDto.getOperator(), cDto.getValue(), cIdx++);
                        group.addCondition(condition);
                    }
                }
                rule.addConditionGroup(group);
            }
        }

        if (input.getActions() != null) {
            int aIdx = 0;
            for (RuleActionDto aDto : input.getActions()) {
                String aId = aDto.getId() != null ? aDto.getId() : "act-" + UUID.randomUUID().toString().substring(0, 8);
                String emailCfgJson = null;
                if (aDto.getEmailConfig() != null) {
                    try {
                        emailCfgJson = objectMapper.writeValueAsString(aDto.getEmailConfig());
                    } catch (JsonProcessingException ignored) {
                    }
                }
                RuleAction action = new RuleAction(aId, rule, aDto.getType(), aDto.getValue(), emailCfgJson, aIdx++);
                rule.addAction(action);
            }
        }
    }

    public RuleResponse mapToResponse(Rule rule) {
        RuleResponse res = new RuleResponse();
        res.setId(rule.getId());
        res.setName(rule.getName());
        res.setDescription(rule.getDescription());
        res.setStatus(rule.getStatus());
        res.setEvaluationOrder(rule.getEvaluationOrder());
        res.setCreatedAt(rule.getCreatedAt());
        res.setUpdatedAt(rule.getUpdatedAt());
        res.setCreatedBy(rule.getCreatedBy());
        res.setMatchCount(rule.getMatchCount());
        res.setLastTriggered(rule.getLastTriggered());

        if (rule.getConditionGroups() != null) {
            for (RuleConditionGroup group : rule.getConditionGroups()) {
                RuleConditionGroupDto gDto = new RuleConditionGroupDto();
                gDto.setId(group.getId());
                gDto.setLogic(group.getLogic());
                if (group.getConditions() != null) {
                    for (RuleCondition cond : group.getConditions()) {
                        gDto.getConditions().add(new RuleConditionDto(cond.getId(), cond.getField(), cond.getOperator(), cond.getValue()));
                    }
                }
                res.getConditionGroups().add(gDto);
            }
        }

        if (rule.getActions() != null) {
            for (RuleAction action : rule.getActions()) {
                EmailConfigDto emailCfg = null;
                if (action.getEmailConfigJson() != null && !action.getEmailConfigJson().isBlank()) {
                    try {
                        emailCfg = objectMapper.readValue(action.getEmailConfigJson(), EmailConfigDto.class);
                    } catch (Exception ignored) {
                    }
                }
                res.getActions().add(new RuleActionDto(action.getId(), action.getType(), action.getValue(), emailCfg));
            }
        }

        return res;
    }
}
