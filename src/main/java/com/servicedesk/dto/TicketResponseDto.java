package com.servicedesk.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.servicedesk.domain.*;
import com.servicedesk.domain.enums.TicketCategory;
import com.servicedesk.domain.enums.TicketPriority;
import com.servicedesk.domain.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketResponseDto {

    private Long id;
    private String ticketNumber;
    private String title;
    private String description;
    private TicketPriority priority;
    private TicketStatus status;
    private TicketCategory category;

    private Long creatorId;
    private String creatorName;
    private String creatorEmail;
    private String creatorRole;

    private Long assignedToId;
    private String assignedToName;
    private String assignedToEmail;
    private String assignedToRole;

    private ResolutionLogDto resolutionLog;
    private BugReportDto bugReport;

    private List<CommentDto> comments = new ArrayList<>();
    private List<HistoryDto> history = new ArrayList<>();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime resolvedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime closedAt;

    public static TicketResponseDto fromEntity(Ticket ticket) {
        TicketResponseDto dto = new TicketResponseDto();
        dto.setId(ticket.getId());
        dto.setTicketNumber(ticket.getTicketNumber());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setPriority(ticket.getPriority());
        dto.setStatus(ticket.getStatus());
        dto.setCategory(ticket.getCategory());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setResolvedAt(ticket.getResolvedAt());
        dto.setClosedAt(ticket.getClosedAt());

        if (ticket.getCreator() != null) {
            dto.setCreatorId(ticket.getCreator().getId());
            dto.setCreatorName(ticket.getCreator().getName());
            dto.setCreatorEmail(ticket.getCreator().getEmail());
            dto.setCreatorRole(ticket.getCreator().getRole().name());
        }

        if (ticket.getAssignedTo() != null) {
            dto.setAssignedToId(ticket.getAssignedTo().getId());
            dto.setAssignedToName(ticket.getAssignedTo().getName());
            dto.setAssignedToEmail(ticket.getAssignedTo().getEmail());
            dto.setAssignedToRole(ticket.getAssignedTo().getRole().name());
        }

        if (ticket.getResolutionLog() != null) {
            ResolutionLog log = ticket.getResolutionLog();
            ResolutionLogDto rDto = new ResolutionLogDto();
            rDto.setId(log.getId());
            rDto.setProblemSummary(log.getProblemSummary());
            rDto.setInvestigationSteps(log.getInvestigationSteps());
            rDto.setRootCause(log.getRootCause());
            rDto.setResolutionApplied(log.getResolutionApplied());
            rDto.setLoggedAt(log.getLoggedAt());
            if (log.getLoggedBy() != null) {
                rDto.setLoggedById(log.getLoggedBy().getId());
                rDto.setLoggedByName(log.getLoggedBy().getName());
            }
            dto.setResolutionLog(rDto);
        }

        if (ticket.getBugReport() != null) {
            BugReport bug = ticket.getBugReport();
            BugReportDto bDto = new BugReportDto();
            bDto.setId(bug.getId());
            bDto.setBugKey(bug.getBugKey());
            bDto.setTitle(bug.getTitle());
            bDto.setSeverity(bug.getSeverity().name());
            bDto.setStatus(bug.getStatus().name());
            bDto.setEnvironment(bug.getEnvironment());
            bDto.setStepsToReproduce(bug.getStepsToReproduce());
            bDto.setExpectedBehavior(bug.getExpectedBehavior());
            bDto.setActualBehavior(bug.getActualBehavior());
            bDto.setStackTrace(bug.getStackTrace());
            bDto.setCreatedAt(bug.getCreatedAt());
            if (bug.getAssignedDeveloper() != null) {
                bDto.setAssignedDeveloperId(bug.getAssignedDeveloper().getId());
                bDto.setAssignedDeveloperName(bug.getAssignedDeveloper().getName());
            }
            dto.setBugReport(bDto);
        }

        if (ticket.getComments() != null) {
            for (TicketComment c : ticket.getComments()) {
                CommentDto cDto = new CommentDto();
                cDto.setId(c.getId());
                cDto.setMessage(c.getMessage());
                cDto.setInternal(c.isInternal());
                cDto.setCreatedAt(c.getCreatedAt());
                if (c.getAuthor() != null) {
                    cDto.setAuthorId(c.getAuthor().getId());
                    cDto.setAuthorName(c.getAuthor().getName());
                    cDto.setAuthorRole(c.getAuthor().getRole().name());
                }
                dto.getComments().add(cDto);
            }
        }

        if (ticket.getHistory() != null) {
            for (TicketHistory h : ticket.getHistory()) {
                HistoryDto hDto = new HistoryDto();
                hDto.setId(h.getId());
                hDto.setFieldChanged(h.getFieldChanged());
                hDto.setOldValue(h.getOldValue());
                hDto.setNewValue(h.getNewValue());
                hDto.setTimestamp(h.getTimestamp());
                if (h.getChangedBy() != null) {
                    hDto.setChangedById(h.getChangedBy().getId());
                    hDto.setChangedByName(h.getChangedBy().getName());
                }
                dto.getHistory().add(hDto);
            }
        }

        return dto;
    }

    public static class ResolutionLogDto {
        private Long id;
        private String problemSummary;
        private String investigationSteps;
        private String rootCause;
        private String resolutionApplied;
        private Long loggedById;
        private String loggedByName;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime loggedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getProblemSummary() { return problemSummary; }
        public void setProblemSummary(String problemSummary) { this.problemSummary = problemSummary; }
        public String getInvestigationSteps() { return investigationSteps; }
        public void setInvestigationSteps(String investigationSteps) { this.investigationSteps = investigationSteps; }
        public String getRootCause() { return rootCause; }
        public void setRootCause(String rootCause) { this.rootCause = rootCause; }
        public String getResolutionApplied() { return resolutionApplied; }
        public void setResolutionApplied(String resolutionApplied) { this.resolutionApplied = resolutionApplied; }
        public Long getLoggedById() { return loggedById; }
        public void setLoggedById(Long loggedById) { this.loggedById = loggedById; }
        public String getLoggedByName() { return loggedByName; }
        public void setLoggedByName(String loggedByName) { this.loggedByName = loggedByName; }
        public LocalDateTime getLoggedAt() { return loggedAt; }
        public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }
    }

    public static class BugReportDto {
        private Long id;
        private String bugKey;
        private String title;
        private String severity;
        private String status;
        private String environment;
        private String stepsToReproduce;
        private String expectedBehavior;
        private String actualBehavior;
        private String stackTrace;
        private Long assignedDeveloperId;
        private String assignedDeveloperName;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getBugKey() { return bugKey; }
        public void setBugKey(String bugKey) { this.bugKey = bugKey; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }
        public String getStepsToReproduce() { return stepsToReproduce; }
        public void setStepsToReproduce(String stepsToReproduce) { this.stepsToReproduce = stepsToReproduce; }
        public String getExpectedBehavior() { return expectedBehavior; }
        public void setExpectedBehavior(String expectedBehavior) { this.expectedBehavior = expectedBehavior; }
        public String getActualBehavior() { return actualBehavior; }
        public void setActualBehavior(String actualBehavior) { this.actualBehavior = actualBehavior; }
        public String getStackTrace() { return stackTrace; }
        public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
        public Long getAssignedDeveloperId() { return assignedDeveloperId; }
        public void setAssignedDeveloperId(Long assignedDeveloperId) { this.assignedDeveloperId = assignedDeveloperId; }
        public String getAssignedDeveloperName() { return assignedDeveloperName; }
        public void setAssignedDeveloperName(String assignedDeveloperName) { this.assignedDeveloperName = assignedDeveloperName; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class CommentDto {
        private Long id;
        private String message;
        private boolean isInternal;
        private Long authorId;
        private String authorName;
        private String authorRole;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public boolean isInternal() { return isInternal; }
        public void setInternal(boolean internal) { isInternal = internal; }
        public Long getAuthorId() { return authorId; }
        public void setAuthorId(Long authorId) { this.authorId = authorId; }
        public String getAuthorName() { return authorName; }
        public void setAuthorName(String authorName) { this.authorName = authorName; }
        public String getAuthorRole() { return authorRole; }
        public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class HistoryDto {
        private Long id;
        private String fieldChanged;
        private String oldValue;
        private String newValue;
        private Long changedById;
        private String changedByName;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime timestamp;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFieldChanged() { return fieldChanged; }
        public void setFieldChanged(String fieldChanged) { this.fieldChanged = fieldChanged; }
        public String getOldValue() { return oldValue; }
        public void setOldValue(String oldValue) { this.oldValue = oldValue; }
        public String getNewValue() { return newValue; }
        public void setNewValue(String newValue) { this.newValue = newValue; }
        public Long getChangedById() { return changedById; }
        public void setChangedById(Long changedById) { this.changedById = changedById; }
        public String getChangedByName() { return changedByName; }
        public void setChangedByName(String changedByName) { this.changedByName = changedByName; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    // Getters and setters for TicketResponseDto
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public TicketCategory getCategory() { return category; }
    public void setCategory(TicketCategory category) { this.category = category; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public String getCreatorEmail() { return creatorEmail; }
    public void setCreatorEmail(String creatorEmail) { this.creatorEmail = creatorEmail; }
    public String getCreatorRole() { return creatorRole; }
    public void setCreatorRole(String creatorRole) { this.creatorRole = creatorRole; }
    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }
    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }
    public String getAssignedToEmail() { return assignedToEmail; }
    public void setAssignedToEmail(String assignedToEmail) { this.assignedToEmail = assignedToEmail; }
    public String getAssignedToRole() { return assignedToRole; }
    public void setAssignedToRole(String assignedToRole) { this.assignedToRole = assignedToRole; }
    public ResolutionLogDto getResolutionLog() { return resolutionLog; }
    public void setResolutionLog(ResolutionLogDto resolutionLog) { this.resolutionLog = resolutionLog; }
    public BugReportDto getBugReport() { return bugReport; }
    public void setBugReport(BugReportDto bugReport) { this.bugReport = bugReport; }
    public List<CommentDto> getComments() { return comments; }
    public void setComments(List<CommentDto> comments) { this.comments = comments; }
    public List<HistoryDto> getHistory() { return history; }
    public void setHistory(List<HistoryDto> history) { this.history = history; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
}
