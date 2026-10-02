package com.e2edocs.controller;

import com.e2edocs.dto.RuleInputDto;
import com.e2edocs.dto.RuleResponse;
import com.e2edocs.dto.RuleTestRequest;
import com.e2edocs.dto.RuleTestResponse;
import com.e2edocs.security.UserPrincipal;
import com.e2edocs.service.RuleEngineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final RuleEngineService ruleEngineService;

    public RuleController(RuleEngineService ruleEngineService) {
        this.ruleEngineService = ruleEngineService;
    }

    @GetMapping
    public ResponseEntity<List<RuleResponse>> getAllRules(@AuthenticationPrincipal UserPrincipal principal) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        List<RuleResponse> rules = ruleEngineService.getAllRules(orgId);
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RuleResponse> getRuleById(@PathVariable String id) {
        RuleResponse rule = ruleEngineService.getRuleById(id);
        return ResponseEntity.ok(rule);
    }

    @PostMapping
    public ResponseEntity<RuleResponse> createRule(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RuleInputDto input) {
        String orgId = principal != null ? principal.getOrganizationId() : "org-001";
        String userName = principal != null ? principal.getName() : "System User";
        RuleResponse rule = ruleEngineService.createRule(orgId, input, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(rule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RuleResponse> updateRule(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody RuleInputDto input) {
        String userName = principal != null ? principal.getName() : "System User";
        RuleResponse rule = ruleEngineService.updateRule(id, input, userName);
        return ResponseEntity.ok(rule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        String userName = principal != null ? principal.getName() : "System User";
        ruleEngineService.deleteRule(id, userName);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/test")
    public ResponseEntity<RuleTestResponse> testRule(@RequestBody RuleTestRequest request) {
        RuleTestResponse response = ruleEngineService.testRule(request);
        return ResponseEntity.ok(response);
    }
}
