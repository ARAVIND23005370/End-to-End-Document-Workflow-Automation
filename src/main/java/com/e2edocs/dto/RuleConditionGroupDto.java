package com.e2edocs.dto;

import com.e2edocs.entity.enums.ConditionLogic;
import java.util.ArrayList;
import java.util.List;

public class RuleConditionGroupDto {
    private String id;
    private ConditionLogic logic = ConditionLogic.AND;
    private List<RuleConditionDto> conditions = new ArrayList<>();

    public RuleConditionGroupDto() {
    }

    public RuleConditionGroupDto(String id, ConditionLogic logic, List<RuleConditionDto> conditions) {
        this.id = id;
        this.logic = logic != null ? logic : ConditionLogic.AND;
        this.conditions = conditions != null ? conditions : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ConditionLogic getLogic() {
        return logic;
    }

    public void setLogic(ConditionLogic logic) {
        this.logic = logic;
    }

    public List<RuleConditionDto> getConditions() {
        return conditions;
    }

    public void setConditions(List<RuleConditionDto> conditions) {
        this.conditions = conditions;
    }
}
