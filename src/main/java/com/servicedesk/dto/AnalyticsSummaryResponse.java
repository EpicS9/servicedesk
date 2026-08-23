package com.servicedesk.dto;

import java.util.Map;

public class AnalyticsSummaryResponse {

    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long waitingForUserTickets;
    private long resolvedTickets;
    private long closedTickets;
    private long criticalTickets;
    private double avgResolutionTimeHours;
    private long totalBugs;
    private long openBugs;

    private Map<String, Long> ticketsByPriority;
    private Map<String, Long> ticketsByCategory;
    private Map<String, Long> engineerWorkload;

    public AnalyticsSummaryResponse() {
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public long getOpenTickets() {
        return openTickets;
    }

    public void setOpenTickets(long openTickets) {
        this.openTickets = openTickets;
    }

    public long getInProgressTickets() {
        return inProgressTickets;
    }

    public void setInProgressTickets(long inProgressTickets) {
        this.inProgressTickets = inProgressTickets;
    }

    public long getWaitingForUserTickets() {
        return waitingForUserTickets;
    }

    public void setWaitingForUserTickets(long waitingForUserTickets) {
        this.waitingForUserTickets = waitingForUserTickets;
    }

    public long getResolvedTickets() {
        return resolvedTickets;
    }

    public void setResolvedTickets(long resolvedTickets) {
        this.resolvedTickets = resolvedTickets;
    }

    public long getClosedTickets() {
        return closedTickets;
    }

    public void setClosedTickets(long closedTickets) {
        this.closedTickets = closedTickets;
    }

    public long getCriticalTickets() {
        return criticalTickets;
    }

    public void setCriticalTickets(long criticalTickets) {
        this.criticalTickets = criticalTickets;
    }

    public double getAvgResolutionTimeHours() {
        return avgResolutionTimeHours;
    }

    public void setAvgResolutionTimeHours(double avgResolutionTimeHours) {
        this.avgResolutionTimeHours = avgResolutionTimeHours;
    }

    public long getTotalBugs() {
        return totalBugs;
    }

    public void setTotalBugs(long totalBugs) {
        this.totalBugs = totalBugs;
    }

    public long getOpenBugs() {
        return openBugs;
    }

    public void setOpenBugs(long openBugs) {
        this.openBugs = openBugs;
    }

    public Map<String, Long> getTicketsByPriority() {
        return ticketsByPriority;
    }

    public void setTicketsByPriority(Map<String, Long> ticketsByPriority) {
        this.ticketsByPriority = ticketsByPriority;
    }

    public Map<String, Long> getTicketsByCategory() {
        return ticketsByCategory;
    }

    public void setTicketsByCategory(Map<String, Long> ticketsByCategory) {
        this.ticketsByCategory = ticketsByCategory;
    }

    public Map<String, Long> getEngineerWorkload() {
        return engineerWorkload;
    }

    public void setEngineerWorkload(Map<String, Long> engineerWorkload) {
        this.engineerWorkload = engineerWorkload;
    }
}
