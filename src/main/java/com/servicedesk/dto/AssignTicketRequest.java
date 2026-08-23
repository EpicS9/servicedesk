package com.servicedesk.dto;

import jakarta.validation.constraints.NotNull;

public class AssignTicketRequest {

    private Long assignedToUserId; // Nullable if unassigning

    @NotNull(message = "Action User ID is required")
    private Long actionUserId;

    public AssignTicketRequest() {
    }

    public AssignTicketRequest(Long assignedToUserId, Long actionUserId) {
        this.assignedToUserId = assignedToUserId;
        this.actionUserId = actionUserId;
    }

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(Long assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }

    public Long getActionUserId() {
        return actionUserId;
    }

    public void setActionUserId(Long actionUserId) {
        this.actionUserId = actionUserId;
    }
}
