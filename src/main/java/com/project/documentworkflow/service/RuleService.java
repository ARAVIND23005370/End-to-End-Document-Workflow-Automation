package com.project.documentworkflow.service;

import com.project.documentworkflow.model.Rule;
import com.project.documentworkflow.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleService {

    @Autowired
    private RuleRepository ruleRepository;

    // Get all rules sorted by priority (1 first)
    public List<Rule> getAllRules() {
        return ruleRepository.findAllByOrderByPriorityAsc();
    }

    public Rule saveRule(Rule rule) {
        return ruleRepository.save(rule);
    }

    public void deleteRule(Long id) {
        ruleRepository.deleteById(id);
    }

    public Rule toggleRule(Long id) {
        Rule rule = ruleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Rule not found"));
        rule.setActive(!rule.getActive());
        return ruleRepository.save(rule);
    }
}
