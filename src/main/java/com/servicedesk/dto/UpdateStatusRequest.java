package com.servicedesk.dto;

import com.servicedesk.domain.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private TicketStatus status;

    @NotNull(message = "Action User ID is required")
    private Long userId;

    public UpdateStatusRequest() {
    }

    public UpdateStatusRequest(TicketStatus status, Long userId) {
        this.status = status;
        this.userId = userId;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
