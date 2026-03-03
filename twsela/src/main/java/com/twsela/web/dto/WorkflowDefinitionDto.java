package com.twsela.web.dto;

import com.twsela.domain.WorkflowDefinition;
import com.twsela.domain.WorkflowStep;

import java.util.List;

/**
 * DTO لإنشاء / تحديث تعريف سلسلة عمل مع خطواتها.
 */
public class WorkflowDefinitionDto {

    private String name;
    private String description;
    private WorkflowDefinition.TriggerEvent triggerEvent;
    private int priority = 5;
    private List<StepDto> steps;

    public static class StepDto {
        private int stepOrder;
        private WorkflowStep.StepType stepType;
        private String configuration;
        private Integer nextStepOnSuccess;
        private Integer nextStepOnFailure;
        private Integer timeoutSeconds;

        public int getStepOrder() { return stepOrder; }
        public void setStepOrder(int stepOrder) { this.stepOrder = stepOrder; }
        public WorkflowStep.StepType getStepType() { return stepType; }
        public void setStepType(WorkflowStep.StepType stepType) { this.stepType = stepType; }
        public String getConfiguration() { return configuration; }
        public void setConfiguration(String configuration) { this.configuration = configuration; }
        public Integer getNextStepOnSuccess() { return nextStepOnSuccess; }
        public void setNextStepOnSuccess(Integer nextStepOnSuccess) { this.nextStepOnSuccess = nextStepOnSuccess; }
        public Integer getNextStepOnFailure() { return nextStepOnFailure; }
        public void setNextStepOnFailure(Integer nextStepOnFailure) { this.nextStepOnFailure = nextStepOnFailure; }
        public Integer getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public WorkflowDefinition.TriggerEvent getTriggerEvent() { return triggerEvent; }
    public void setTriggerEvent(WorkflowDefinition.TriggerEvent triggerEvent) { this.triggerEvent = triggerEvent; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public List<StepDto> getSteps() { return steps; }
    public void setSteps(List<StepDto> steps) { this.steps = steps; }
}
