package com.twsela.web.dto;

import com.twsela.domain.AutomationRule;
import com.twsela.domain.WorkflowDefinition;

/**
 * DTO لإنشاء / تحديث قاعدة أتمتة.
 */
public class AutomationRuleDto {

    private String name;
    private String description;
    private WorkflowDefinition.TriggerEvent triggerEvent;
    private String conditionExpression;
    private AutomationRule.ActionType actionType;
    private String actionConfig;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public WorkflowDefinition.TriggerEvent getTriggerEvent() { return triggerEvent; }
    public void setTriggerEvent(WorkflowDefinition.TriggerEvent triggerEvent) { this.triggerEvent = triggerEvent; }

    public String getConditionExpression() { return conditionExpression; }
    public void setConditionExpression(String conditionExpression) { this.conditionExpression = conditionExpression; }

    public AutomationRule.ActionType getActionType() { return actionType; }
    public void setActionType(AutomationRule.ActionType actionType) { this.actionType = actionType; }

    public String getActionConfig() { return actionConfig; }
    public void setActionConfig(String actionConfig) { this.actionConfig = actionConfig; }
}
