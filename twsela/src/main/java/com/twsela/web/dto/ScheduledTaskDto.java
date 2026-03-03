package com.twsela.web.dto;

import com.twsela.domain.ScheduledTask;

/**
 * DTO لإنشاء / تحديث مهمة مجدولة.
 */
public class ScheduledTaskDto {

    private String name;
    private String description;
    private ScheduledTask.TaskType taskType;
    private String cronExpression;
    private String configuration;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ScheduledTask.TaskType getTaskType() { return taskType; }
    public void setTaskType(ScheduledTask.TaskType taskType) { this.taskType = taskType; }

    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

    public String getConfiguration() { return configuration; }
    public void setConfiguration(String configuration) { this.configuration = configuration; }
}
