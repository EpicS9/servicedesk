package com.servicedesk.dto;

import com.servicedesk.domain.enums.BugSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ConvertToBugRequest {

    @NotBlank(message = "Bug title is required")
    private String title;

    @NotNull(message = "Severity is required")
    private BugSeverity severity;

    @NotBlank(message = "Environment is required (e.g. Production, Staging)")
    private String environment;

    @NotBlank(message = "Steps to reproduce are required")
    private String stepsToReproduce;

    @NotBlank(message = "Expected behavior is required")
    private String expectedBehavior;

    @NotBlank(message = "Actual behavior is required")
    private String actualBehavior;

    private String stackTrace;

    private Long assignedDeveloperId;

    public ConvertToBugRequest() {
    }

    public ConvertToBugRequest(String title, BugSeverity severity, String environment,
                               String stepsToReproduce, String expectedBehavior,
                               String actualBehavior, String stackTrace, Long assignedDeveloperId) {
        this.title = title;
        this.severity = severity;
        this.environment = environment;
        this.stepsToReproduce = stepsToReproduce;
        this.expectedBehavior = expectedBehavior;
        this.actualBehavior = actualBehavior;
        this.stackTrace = stackTrace;
        this.assignedDeveloperId = assignedDeveloperId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BugSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(BugSeverity severity) {
        this.severity = severity;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getStepsToReproduce() {
        return stepsToReproduce;
    }

    public void setStepsToReproduce(String stepsToReproduce) {
        this.stepsToReproduce = stepsToReproduce;
    }

    public String getExpectedBehavior() {
        return expectedBehavior;
    }

    public void setExpectedBehavior(String expectedBehavior) {
        this.expectedBehavior = expectedBehavior;
    }

    public String getActualBehavior() {
        return actualBehavior;
    }

    public void setActualBehavior(String actualBehavior) {
        this.actualBehavior = actualBehavior;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public Long getAssignedDeveloperId() {
        return assignedDeveloperId;
    }

    public void setAssignedDeveloperId(Long assignedDeveloperId) {
        this.assignedDeveloperId = assignedDeveloperId;
    }
}
