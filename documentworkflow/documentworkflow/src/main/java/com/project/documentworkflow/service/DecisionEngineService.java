package com.project.documentworkflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

import com.project.documentworkflow.model.*;
import com.project.documentworkflow.repository.*;

@Service
public class DecisionEngineService {

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private DecisionRepository decisionRepository;

    public Decision evaluateDecision(Document document, OCRData ocrData) {

        List<Rule> rules = ruleRepository.findAll();

        String finalDecision = "APPROVE";

        for (Rule rule : rules) {
            if (ocrData.getConfidenceScore() < rule.getThresholdValue()) {
                finalDecision = "REVIEW";
                break;
            }
        }

        Decision decision = new Decision();
        decision.setDecisionType(finalDecision);
        decision.setDecisionSource("SYSTEM");
        decision.setDecisionTime(LocalDateTime.now());
        decision.setDocument(document);

        return decisionRepository.save(decision);
    }
}
