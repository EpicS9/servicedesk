package com.servicedesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ResolutionLogRequest {

    @NotBlank(message = "Problem summary is required")
    @Size(max = 500, message = "Problem summary must not exceed 500 characters")
    private String problemSummary;

    @NotBlank(message = "Investigation steps are required")
    private String investigationSteps;

    @NotBlank(message = "Root cause is required")
    private String rootCause;

    @NotBlank(message = "Resolution applied is required")
    private String resolutionApplied;

    @NotNull(message = "Logged by user ID is required")
    private Long loggedById;

    public ResolutionLogRequest() {
    }

    public ResolutionLogRequest(String problemSummary, String investigationSteps, String rootCause, String resolutionApplied, Long loggedById) {
        this.problemSummary = problemSummary;
        this.investigationSteps = investigationSteps;
        this.rootCause = rootCause;
        this.resolutionApplied = resolutionApplied;
        this.loggedById = loggedById;
    }

    public String getProblemSummary() {
        return problemSummary;
    }

    public void setProblemSummary(String problemSummary) {
        this.problemSummary = problemSummary;
    }

    public String getInvestigationSteps() {
        return investigationSteps;
    }

    public void setInvestigationSteps(String investigationSteps) {
        this.investigationSteps = investigationSteps;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public String getResolutionApplied() {
        return resolutionApplied;
    }

    public void setResolutionApplied(String resolutionApplied) {
        this.resolutionApplied = resolutionApplied;
    }

    public Long getLoggedById() {
        return loggedById;
    }

    public void setLoggedById(Long loggedById) {
        this.loggedById = loggedById;
    }
}
