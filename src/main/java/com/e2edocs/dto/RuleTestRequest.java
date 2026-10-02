package com.e2edocs.dto;

import java.util.Map;

public class RuleTestRequest {
    private RuleInputDto rule;
    private DocumentResponse document;
    private Map<String, Object> testContext;

    public RuleTestRequest() {
    }

    public RuleTestRequest(RuleInputDto rule, DocumentResponse document) {
        this.rule = rule;
        this.document = document;
    }

    public RuleInputDto getRule() {
        return rule;
    }

    public void setRule(RuleInputDto rule) {
        this.rule = rule;
    }

    public DocumentResponse getDocument() {
        return document;
    }

    public void setDocument(DocumentResponse document) {
        this.document = document;
    }

    public Map<String, Object> getTestContext() {
        return testContext;
    }

    public void setTestContext(Map<String, Object> testContext) {
        this.testContext = testContext;
    }
}
