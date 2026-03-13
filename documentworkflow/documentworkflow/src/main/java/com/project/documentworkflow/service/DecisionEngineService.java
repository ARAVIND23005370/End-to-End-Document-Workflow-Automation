package com.project.documentworkflow.service;

import com.project.documentworkflow.exception.DocumentNotFoundException;
import com.project.documentworkflow.model.Decision;
import com.project.documentworkflow.model.Document;
import com.project.documentworkflow.model.OCRData;
import com.project.documentworkflow.model.Rule;
import com.project.documentworkflow.repository.DecisionRepository;
import com.project.documentworkflow.repository.DocumentRepository;
import com.project.documentworkflow.repository.OCRDataRepository;
import com.project.documentworkflow.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class DecisionEngineService {

    private static final Logger log = LoggerFactory.getLogger(DecisionEngineService.class);

    @Autowired private DocumentRepository documentRepository;
    @Autowired private OCRDataRepository ocrDataRepository;
    @Autowired private RuleRepository ruleRepository;
    @Autowired private DecisionRepository decisionRepository;
    @Autowired private AuditService auditService;
    @Autowired private EmailNotificationService emailNotificationService;

    @Transactional
    public Decision evaluateDecision(Long documentId, Long ocrDataId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        OCRData ocrData = ocrDataRepository.findById(ocrDataId)
                .orElseThrow(() -> new RuntimeException("OCR data not found"));

        // Get active rules sorted by priority
        List<Rule> allRules = ruleRepository.findByActiveTrueOrderByPriorityAsc();

        // Filter rules that apply to this document type OR apply to ALL
        String docType = document.getDocumentType() != null ?
                document.getDocumentType().toUpperCase() : "GENERAL";

        List<Rule> applicableRules = allRules.stream()
                .filter(r -> r.getDocumentType() == null ||
                             r.getDocumentType().equalsIgnoreCase("ALL") ||
                             r.getDocumentType().equalsIgnoreCase(docType))
                .toList();

        // Assign folder and priority
        document.setFolderPath(assignFolder(document.getDocumentType()));
        document.setPriority(assignPriority(document.getDocumentType()));

        Decision decision = new Decision();
        decision.setDecisionSource("SYSTEM");
        decision.setDocument(document);

        double confidenceScore = ocrData.getConfidenceScore();
        String extractedText = ocrData.getExtractedText() != null ?
                ocrData.getExtractedText().toLowerCase() : "";

        log.info("Evaluating document ID {} | Type: {} | Confidence: {}% | Applicable rules: {}",
                documentId, docType,
                String.format("%.0f", confidenceScore * 100),
                applicableRules.size());

        if (applicableRules.isEmpty()) {
            // No rules for this document type = auto approve
            decision.setDecisionType("APPROVED");
            decision.setDecisionReason("No rules configured for type: " + docType + " — auto approved");
            document.setStatus("APPROVED");
            auditService.log(documentId, "APPROVED", "SYSTEM",
                    "No applicable rules for " + docType + " — auto approved");

        } else {
            boolean failed = false;
            String failedRuleName = "";
            String failedReason = "";

            for (Rule rule : applicableRules) {

                // ── Check 1: Confidence threshold ────────────────────────
                if (confidenceScore < rule.getThresholdValue()) {
                    failed = true;
                    failedRuleName = "Priority " + rule.getPriority() + " — " + rule.getRuleName();
                    failedReason = "Confidence score " +
                            String.format("%.0f", confidenceScore * 100) +
                            "% is below required threshold of " +
                            String.format("%.0f", rule.getThresholdValue() * 100) + "%";
                    log.info("Rule FAILED (threshold): {} | Score: {}% < {}%",
                            rule.getRuleName(),
                            String.format("%.0f", confidenceScore * 100),
                            String.format("%.0f", rule.getThresholdValue() * 100));
                    break;
                }

                // ── Check 2: Required keywords (ALL must be present) ─────
                if (rule.getRequiredKeywords() != null &&
                    !rule.getRequiredKeywords().trim().isEmpty()) {

                    String[] keywords = rule.getRequiredKeywords()
                            .toLowerCase()
                            .split(",");

                    for (String keyword : keywords) {
                        String kw = keyword.trim();
                        if (!kw.isEmpty() && !extractedText.contains(kw)) {
                            failed = true;
                            failedRuleName = "Priority " + rule.getPriority() +
                                    " — " + rule.getRuleName();
                            failedReason = "Required keyword '" + kw +
                                    "' not found in document. All keywords must be present: [" +
                                    rule.getRequiredKeywords() + "]";
                            log.info("Rule FAILED (keyword): {} | Missing keyword: '{}'",
                                    rule.getRuleName(), kw);
                            break;
                        }
                    }

                    if (failed) break;
                }

                log.info("Rule PASSED: {} | Score: {}%",
                        rule.getRuleName(),
                        String.format("%.0f", confidenceScore * 100));
            }

            if (failed) {
                // Check if score is in REVIEW zone (30% to threshold)
                if (confidenceScore >= 0.30 && confidenceScore < 0.70 &&
                    failedReason.contains("below required threshold")) {
                    // Borderline score — send for manual review
                    decision.setDecisionType("REVIEW");
                    decision.setDecisionSource("SYSTEM_FLAGGED");
                    decision.setDecisionReason("Confidence score " +
                            String.format("%.0f", confidenceScore * 100) +
                            "% is borderline — requires manual review. Rule: " + failedRuleName);
                    document.setStatus("REVIEW");
                    auditService.log(documentId, "REVIEW", "SYSTEM",
                            "Flagged for manual review — confidence: " +
                            String.format("%.0f", confidenceScore * 100) + "% | Rule: " + failedRuleName);
                    log.info("Document {} flagged for REVIEW — borderline score {}%",
                            documentId, String.format("%.0f", confidenceScore * 100));

                } else {
                    // Clear failure — reject
                    decision.setDecisionType("REJECTED");
                    decision.setDecisionSource("FAILED_RULE: " + failedRuleName);
                    decision.setDecisionReason(failedReason);
                    document.setStatus("REJECTED");
                    auditService.log(documentId, "REJECTED", "SYSTEM",
                            "Rejected — " + failedReason);
                    emailNotificationService.sendRejectionEmailIfAllowed(document, failedRuleName);
                    log.info("Document {} REJECTED — {}", documentId, failedReason);
                }

            } else {
                decision.setDecisionType("APPROVED");
                decision.setDecisionReason("Passed all " + applicableRules.size() +
                        " rules for document type: " + docType);
                document.setStatus("APPROVED");
                auditService.log(documentId, "APPROVED", "SYSTEM",
                        "Passed all " + applicableRules.size() + " rules for " + docType);
                log.info("Document {} APPROVED — passed all {} rules", documentId, applicableRules.size());
            }
        }

        decisionRepository.save(decision);
        documentRepository.save(document);
        return decision;
    }

    private String assignFolder(String documentType) {
        if (documentType == null) return "uploads/UNKNOWN/";
        switch (documentType.toUpperCase()) {
            case "INVOICE":   return "uploads/INVOICES/";
            case "COMPLAINT": return "uploads/COMPLAINTS/";
            case "APPLICATION": return "uploads/APPLICATIONS/";
            case "REPORT":    return "uploads/REPORTS/";
            case "CONTRACT":  return "uploads/CONTRACTS/";
            case "IDENTITY":  return "uploads/IDENTITY/";
            case "LOAN":      return "uploads/LOANS/";
            case "MORTGAGE":  return "uploads/MORTGAGES/";
            case "FINANCIAL": return "uploads/FINANCIAL/";
            case "MEDICAL":   return "uploads/MEDICAL/";
            case "STUDENT":   return "uploads/STUDENT/";
            case "HR":        return "uploads/HR/";
            case "LEGAL":     return "uploads/LEGAL/";
            default:          return "uploads/GENERAL/";
        }
    }

    private int assignPriority(String documentType) {
        if (documentType == null) return 3;
        switch (documentType.toUpperCase()) {
            case "COMPLAINT":
            case "IDENTITY":
            case "LEGAL":
            case "MEDICAL":   return 1;
            case "INVOICE":
            case "CONTRACT":
            case "LOAN":
            case "HR":        return 2;
            default:          return 3;
        }
    }
}
