package com.servicedesk.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing troubleshooting and root cause analysis.
 * Captures the technical investigation journey directly addressing JD requirements.
 */
@Entity
@Table(name = "resolution_logs")
public class ResolutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true)
    private Ticket ticket;

    @Column(name = "problem_summary", nullable = false, length = 500)
    private String problemSummary;

    @Column(name = "investigation_steps", nullable = false, columnDefinition = "TEXT")
    private String investigationSteps;

    @Column(name = "root_cause", nullable = false, columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "resolution_applied", nullable = false, columnDefinition = "TEXT")
    private String resolutionApplied;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "logged_by_id", nullable = false)
    private User loggedBy;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt = LocalDateTime.now();

    public ResolutionLog() {
    }

    public ResolutionLog(Ticket ticket, String problemSummary, String investigationSteps,
                         String rootCause, String resolutionApplied, User loggedBy) {
        this.ticket = ticket;
        this.problemSummary = problemSummary;
        this.investigationSteps = investigationSteps;
        this.rootCause = rootCause;
        this.resolutionApplied = resolutionApplied;
        this.loggedBy = loggedBy;
        this.loggedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
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

    public User getLoggedBy() {
        return loggedBy;
    }

    public void setLoggedBy(User loggedBy) {
        this.loggedBy = loggedBy;
    }

    public LocalDateTime getLoggedAt() {
        return loggedAt;
    }

    public void setLoggedAt(LocalDateTime loggedAt) {
        this.loggedAt = loggedAt;
    }
}
