package com.servicedesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateCommentRequest {

    @NotNull(message = "Author ID is required")
    private Long authorId;

    @NotBlank(message = "Comment message cannot be empty")
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String message;

    private boolean isInternal = false;

    public CreateCommentRequest() {
    }

    public CreateCommentRequest(Long authorId, String message, boolean isInternal) {
        this.authorId = authorId;
        this.message = message;
        this.isInternal = isInternal;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isInternal() {
        return isInternal;
    }

    public void setInternal(boolean internal) {
        isInternal = internal;
    }
}
