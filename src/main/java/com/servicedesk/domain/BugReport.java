package com.servicedesk.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.servicedesk.domain.enums.BugSeverity;
import com.servicedesk.domain.enums.BugStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for QA & Bug Tracking Workflow.
 * Allows support tickets to be escalated to developers as bug records.
 */
@Entity
@Table(name = "bug_reports")
public class BugReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bug_key", nullable = false, unique = true)
    private String bugKey; // e.g. "BUG-102"

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true)
    private Ticket ticket;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BugSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BugStatus status = BugStatus.OPEN;

    @Column(nullable = false)
    private String environment; // "Production", "Staging", "QA"

    @Column(name = "steps_to_reproduce", nullable = false, columnDefinition = "TEXT")
    private String stepsToReproduce;

    @Column(name = "expected_behavior", nullable = false, columnDefinition = "TEXT")
    private String expectedBehavior;

    @Column(name = "actual_behavior", nullable = false, columnDefinition = "TEXT")
    private String actualBehavior;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_developer_id")
    private Developer assignedDeveloper;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public BugReport() {
    }

    public BugReport(String bugKey, Ticket ticket, String title, BugSeverity severity,
                     String environment, String stepsToReproduce, String expectedBehavior,
                     String actualBehavior, String stackTrace, Developer assignedDeveloper) {
        this.bugKey = bugKey;
        this.ticket = ticket;
        this.title = title;
        this.severity = severity;
        this.environment = environment;
        this.stepsToReproduce = stepsToReproduce;
        this.expectedBehavior = expectedBehavior;
        this.actualBehavior = actualBehavior;
        this.stackTrace = stackTrace;
        this.assignedDeveloper = assignedDeveloper;
        this.status = BugStatus.OPEN;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBugKey() {
        return bugKey;
    }

    public void setBugKey(String bugKey) {
        this.bugKey = bugKey;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
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

    public BugStatus getStatus() {
        return status;
    }

    public void setStatus(BugStatus status) {
        this.status = status;
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

    public Developer getAssignedDeveloper() {
        return assignedDeveloper;
    }

    public void setAssignedDeveloper(Developer assignedDeveloper) {
        this.assignedDeveloper = assignedDeveloper;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
