package com.e2edocs.dto;

import java.util.ArrayList;
import java.util.List;

public class RuleTestResponse {
    private boolean matched;
    private List<String> matchedConditions = new ArrayList<>();
    private List<String> unmatchedConditions = new ArrayList<>();
    private List<RuleActionDto> resultingActions = new ArrayList<>();
    private List<String> logs = new ArrayList<>();

    public RuleTestResponse() {
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public List<String> getMatchedConditions() {
        return matchedConditions;
    }

    public void setMatchedConditions(List<String> matchedConditions) {
        this.matchedConditions = matchedConditions;
    }

    public List<String> getUnmatchedConditions() {
        return unmatchedConditions;
    }

    public void setUnmatchedConditions(List<String> unmatchedConditions) {
        this.unmatchedConditions = unmatchedConditions;
    }

    public List<RuleActionDto> getResultingActions() {
        return resultingActions;
    }

    public void setResultingActions(List<RuleActionDto> resultingActions) {
        this.resultingActions = resultingActions;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }
}
