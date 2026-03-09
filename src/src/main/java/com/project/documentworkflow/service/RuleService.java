package com.project.documentworkflow.service;

import com.project.documentworkflow.model.Rule;
import com.project.documentworkflow.repository.RuleRepository;
import org.springframework.stereotype.Service;

@Service
public class RuleService {

    private final RuleRepository ruleRepository;

    public RuleService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    public Rule saveRule(Rule rule) {
        return ruleRepository.save(rule);
    }
}
