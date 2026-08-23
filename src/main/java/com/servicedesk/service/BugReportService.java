package com.servicedesk.service;

import com.servicedesk.domain.BugReport;
import com.servicedesk.domain.Developer;
import com.servicedesk.domain.Ticket;
import com.servicedesk.domain.User;
import com.servicedesk.domain.enums.BugStatus;
import com.servicedesk.domain.enums.Role;
import com.servicedesk.dto.ConvertToBugRequest;
import com.servicedesk.dto.TicketResponseDto;
import com.servicedesk.exception.InvalidOperationException;
import com.servicedesk.exception.ResourceNotFoundException;
import com.servicedesk.repository.BugReportRepository;
import com.servicedesk.repository.TicketRepository;
import com.servicedesk.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Transactional
public class BugReportService {

    private final TicketRepository ticketRepository;
    private final BugReportRepository bugReportRepository;
    private final UserRepository userRepository;

    private static final AtomicLong BUG_SEQUENCE = new AtomicLong(300);

    public BugReportService(TicketRepository ticketRepository, BugReportRepository bugReportRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.bugReportRepository = bugReportRepository;
        this.userRepository = userRepository;
    }

    public TicketResponseDto convertTicketToBug(Long ticketId, ConvertToBugRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        if (ticket.getBugReport() != null) {
            throw new InvalidOperationException("Ticket " + ticket.getTicketNumber() + " is already linked to bug " + ticket.getBugReport().getBugKey());
        }

        Developer developer = null;
        if (request.getAssignedDeveloperId() != null) {
            User user = userRepository.findById(request.getAssignedDeveloperId())
                    .orElseThrow(() -> new ResourceNotFoundException("Developer not found with ID: " + request.getAssignedDeveloperId()));
            if (!(user instanceof Developer)) {
                throw new InvalidOperationException("Assigned user must be a Developer. Provided: " + user.getRole());
            }
            developer = (Developer) user;
        }

        String bugKey = "BUG-" + BUG_SEQUENCE.incrementAndGet();
        BugReport bugReport = new BugReport(
                bugKey,
                ticket,
                request.getTitle(),
                request.getSeverity(),
                request.getEnvironment(),
                request.getStepsToReproduce(),
                request.getExpectedBehavior(),
                request.getActualBehavior(),
                request.getStackTrace(),
                developer
        );

        ticket.attachBugReport(bugReport);
        ticket.addHistory(developer != null ? developer : ticket.getCreator(), "bugReport", null, "Escalated to " + bugKey);

        bugReportRepository.save(bugReport);
        Ticket saved = ticketRepository.save(ticket);
        return TicketResponseDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<TicketResponseDto.BugReportDto> getAllBugs() {
        return bugReportRepository.findAllByOrderByCreatedAtDesc().stream().map(bug -> {
            TicketResponseDto.BugReportDto dto = new TicketResponseDto.BugReportDto();
            dto.setId(bug.getId());
            dto.setBugKey(bug.getBugKey());
            dto.setTitle(bug.getTitle());
            dto.setSeverity(bug.getSeverity().name());
            dto.setStatus(bug.getStatus().name());
            dto.setEnvironment(bug.getEnvironment());
            dto.setStepsToReproduce(bug.getStepsToReproduce());
            dto.setExpectedBehavior(bug.getExpectedBehavior());
            dto.setActualBehavior(bug.getActualBehavior());
            dto.setStackTrace(bug.getStackTrace());
            dto.setCreatedAt(bug.getCreatedAt());
            if (bug.getAssignedDeveloper() != null) {
                dto.setAssignedDeveloperId(bug.getAssignedDeveloper().getId());
                dto.setAssignedDeveloperName(bug.getAssignedDeveloper().getName());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public TicketResponseDto.BugReportDto updateBugStatus(Long bugId, BugStatus newStatus) {
        BugReport bug = bugReportRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with ID: " + bugId));

        bug.setStatus(newStatus);
        if (newStatus == BugStatus.VERIFIED || newStatus == BugStatus.CLOSED) {
            bug.setResolvedAt(LocalDateTime.now());
        }
        BugReport saved = bugReportRepository.save(bug);

        TicketResponseDto.BugReportDto dto = new TicketResponseDto.BugReportDto();
        dto.setId(saved.getId());
        dto.setBugKey(saved.getBugKey());
        dto.setTitle(saved.getTitle());
        dto.setSeverity(saved.getSeverity().name());
        dto.setStatus(saved.getStatus().name());
        dto.setEnvironment(saved.getEnvironment());
        dto.setStepsToReproduce(saved.getStepsToReproduce());
        dto.setExpectedBehavior(saved.getExpectedBehavior());
        dto.setActualBehavior(saved.getActualBehavior());
        dto.setStackTrace(saved.getStackTrace());
        dto.setCreatedAt(saved.getCreatedAt());
        if (saved.getAssignedDeveloper() != null) {
            dto.setAssignedDeveloperId(saved.getAssignedDeveloper().getId());
            dto.setAssignedDeveloperName(saved.getAssignedDeveloper().getName());
        }
        return dto;
    }
}
