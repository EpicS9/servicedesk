package com.servicedesk.service;

import com.servicedesk.domain.Ticket;
import com.servicedesk.domain.enums.BugStatus;
import com.servicedesk.domain.enums.TicketPriority;
import com.servicedesk.domain.enums.TicketStatus;
import com.servicedesk.dto.AnalyticsSummaryResponse;
import com.servicedesk.repository.BugReportRepository;
import com.servicedesk.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final TicketRepository ticketRepository;
    private final BugReportRepository bugReportRepository;

    public AnalyticsService(TicketRepository ticketRepository, BugReportRepository bugReportRepository) {
        this.ticketRepository = ticketRepository;
        this.bugReportRepository = bugReportRepository;
    }

    public AnalyticsSummaryResponse getDashboardSummary() {
        AnalyticsSummaryResponse summary = new AnalyticsSummaryResponse();

        long total = ticketRepository.count();
        long open = ticketRepository.countByStatus(TicketStatus.OPEN);
        long inProgress = ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        long waiting = ticketRepository.countByStatus(TicketStatus.WAITING_FOR_USER);
        long resolved = ticketRepository.countByStatus(TicketStatus.RESOLVED);
        long closed = ticketRepository.countByStatus(TicketStatus.CLOSED);
        long critical = ticketRepository.countByPriorityAndStatusNot(TicketPriority.CRITICAL, TicketStatus.CLOSED);

        long totalBugs = bugReportRepository.count();
        long openBugs = bugReportRepository.countByStatus(BugStatus.OPEN) +
                        bugReportRepository.countByStatus(BugStatus.IN_TRIAGE) +
                        bugReportRepository.countByStatus(BugStatus.IN_FIX);

        summary.setTotalTickets(total);
        summary.setOpenTickets(open);
        summary.setInProgressTickets(inProgress);
        summary.setWaitingForUserTickets(waiting);
        summary.setResolvedTickets(resolved);
        summary.setClosedTickets(closed);
        summary.setCriticalTickets(critical);
        summary.setTotalBugs(totalBugs);
        summary.setOpenBugs(openBugs);

        // Calculate Average Resolution Time
        List<Ticket> resolvedTickets = ticketRepository.findResolvedTickets();
        if (!resolvedTickets.isEmpty()) {
            double totalDurationHours = 0;
            int count = 0;
            for (Ticket t : resolvedTickets) {
                if (t.getCreatedAt() != null && t.getResolvedAt() != null) {
                    long minutes = Duration.between(t.getCreatedAt(), t.getResolvedAt()).toMinutes();
                    totalDurationHours += (minutes / 60.0);
                    count++;
                }
            }
            summary.setAvgResolutionTimeHours(count > 0 ? Math.round((totalDurationHours / count) * 10.0) / 10.0 : 0.0);
        } else {
            summary.setAvgResolutionTimeHours(0.0);
        }

        // Aggregate tickets by priority
        Map<String, Long> priorityMap = new HashMap<>();
        for (Object[] row : ticketRepository.countOpenTicketsByPriority()) {
            priorityMap.put(row[0].toString(), (Long) row[1]);
        }
        summary.setTicketsByPriority(priorityMap);

        // Aggregate tickets by category
        Map<String, Long> categoryMap = new HashMap<>();
        for (Object[] row : ticketRepository.countTicketsByCategory()) {
            categoryMap.put(row[0].toString(), (Long) row[1]);
        }
        summary.setTicketsByCategory(categoryMap);

        // Support engineer workload
        Map<String, Long> workloadMap = new HashMap<>();
        for (Object[] row : ticketRepository.countWorkloadByEngineer()) {
            workloadMap.put((String) row[0], (Long) row[1]);
        }
        summary.setEngineerWorkload(workloadMap);

        return summary;
    }
}
