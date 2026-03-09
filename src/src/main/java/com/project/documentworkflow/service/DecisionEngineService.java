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

import java.util.List;

@Service
public class DecisionEngineService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private OCRDataRepository ocrDataRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private DecisionRepository decisionRepository;

    @Transactional
    public Decision evaluateDecision(Long documentId, Long ocrDataId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));


        OCRData ocrData = ocrDataRepository.findById(ocrDataId)
                .orElseThrow(() -> new RuntimeException("OCR data not found"));

        List<Rule> rules = ruleRepository.findAll();

        if (rules.isEmpty()) {
            throw new RuntimeException("No rules configured in system");
        }

        Decision decision = new Decision();
        decision.setDecisionSource("SYSTEM");
        decision.setDocument(document);

        boolean failed = false;
        String failedRuleName = "";

        for (Rule rule : rules) {
            if (ocrData.getConfidenceScore() < rule.getThresholdValue()) {
                failed = true;
                failedRuleName = rule.getRuleName();
                break;
            }
        }

        if (failed) {
            decision.setDecisionType("REVIEW");
            decision.setDecisionSource("FAILED_RULE: " + failedRuleName);
            document.setStatus("UNDER_REVIEW");
        } else {
            decision.setDecisionType("APPROVE");
            document.setStatus("APPROVED");
        }

        Decision savedDecision = decisionRepository.save(decision);
        documentRepository.save(document);

        return savedDecision;
    }
}
