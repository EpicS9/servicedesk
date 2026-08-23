package com.servicedesk.domain;

import com.servicedesk.domain.enums.TicketCategory;
import com.servicedesk.domain.enums.TicketPriority;
import com.servicedesk.domain.enums.TicketStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Core Ticket Aggregate Entity.
 * Encapsulates ticket lifecycle, assignment rules, comments, history, and troubleshooting logs.
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_number", nullable = false, unique = true, length = 30)
    private String ticketNumber;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketCategory category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @OneToOne(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private ResolutionLog resolutionLog;

    @OneToOne(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private BugReport bugReport;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<TicketComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("timestamp DESC")
    private List<TicketHistory> history = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    public Ticket() {
    }

    public Ticket(String ticketNumber, String title, String description,
                  TicketPriority priority, TicketCategory category, User creator) {
        this.ticketNumber = ticketNumber;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.category = category;
        this.creator = creator;
        this.status = TicketStatus.OPEN;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Business Methods (Encapsulation)
    public void assignTo(User user, User performedBy) {
        if (user != null && !user.canBeAssignedTickets()) {
            throw new IllegalArgumentException("User with role " + user.getRole() + " cannot be assigned support tickets.");
        }
        String oldVal = this.assignedTo != null ? this.assignedTo.getName() : "UNASSIGNED";
        this.assignedTo = user;
        if (this.status == TicketStatus.OPEN && user != null) {
            this.status = TicketStatus.IN_PROGRESS;
        }
        this.updatedAt = LocalDateTime.now();
        this.addHistory(performedBy, "assignedTo", oldVal, user != null ? user.getName() : "UNASSIGNED");
    }

    public void updateStatus(TicketStatus newStatus, User performedBy) {
        if (this.status == newStatus) return;
        String oldVal = this.status.name();
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();

        if (newStatus == TicketStatus.RESOLVED && this.resolvedAt == null) {
            this.resolvedAt = LocalDateTime.now();
        } else if (newStatus == TicketStatus.CLOSED) {
            this.closedAt = LocalDateTime.now();
            if (this.resolvedAt == null) {
                this.resolvedAt = LocalDateTime.now();
            }
        }
        this.addHistory(performedBy, "status", oldVal, newStatus.name());
    }

    public void addComment(TicketComment comment) {
        comments.add(comment);
        comment.setTicket(this);
        this.updatedAt = LocalDateTime.now();
    }

    public void addHistory(User performedBy, String field, String oldVal, String newVal) {
        TicketHistory hist = new TicketHistory(this, performedBy, field, oldVal, newVal);
        history.add(hist);
    }

    public void attachResolutionLog(ResolutionLog log) {
        this.resolutionLog = log;
        log.setTicket(this);
        this.status = TicketStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void attachBugReport(BugReport bug) {
        this.bugReport = bug;
        bug.setTicket(this);
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public ResolutionLog getResolutionLog() {
        return resolutionLog;
    }

    public void setResolutionLog(ResolutionLog resolutionLog) {
        this.resolutionLog = resolutionLog;
    }

    public BugReport getBugReport() {
        return bugReport;
    }

    public void setBugReport(BugReport bugReport) {
        this.bugReport = bugReport;
    }

    public List<TicketComment> getComments() {
        return comments;
    }

    public void setComments(List<TicketComment> comments) {
        this.comments = comments;
    }

    public List<TicketHistory> getHistory() {
        return history;
    }

    public void setHistory(List<TicketHistory> history) {
        this.history = history;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }
}
