package com.project.documentworkflow.controller;

import com.project.documentworkflow.dto.ApiResponse;
import com.project.documentworkflow.model.Rule;
import com.project.documentworkflow.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    @Autowired
    private RuleService ruleService;

    // GET all rules sorted by priority
    @GetMapping
    public ApiResponse<List<Rule>> getAllRules() {
        return new ApiResponse<>(true, ruleService.getAllRules(), null);
    }

    // POST create new rule
    @PostMapping
    public ApiResponse<Rule> createRule(@RequestBody Rule rule) {
        return new ApiResponse<>(true, ruleService.saveRule(rule), null);
    }

    // DELETE a rule by id
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteRule(@PathVariable Long id) {
        ruleService.deleteRule(id);
        return new ApiResponse<>(true, "Rule deleted", null);
    }

    // PUT toggle rule active/inactive
    @PutMapping("/{id}/toggle")
    public ApiResponse<Rule> toggleRule(@PathVariable Long id) {
        return new ApiResponse<>(true, ruleService.toggleRule(id), null);
    }
}
