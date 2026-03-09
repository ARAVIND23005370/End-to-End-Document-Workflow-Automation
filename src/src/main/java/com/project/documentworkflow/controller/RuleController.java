package com.project.documentworkflow.controller;

import com.project.documentworkflow.model.Rule;
import com.project.documentworkflow.service.RuleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @PostMapping
    public Rule createRule(@RequestBody Rule rule) {
        return ruleService.saveRule(rule);
    }
}
