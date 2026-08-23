package com.servicedesk.service;

import com.servicedesk.domain.ResolutionLog;
import com.servicedesk.domain.Ticket;
import com.servicedesk.domain.User;
import com.servicedesk.domain.enums.TicketStatus;
import com.servicedesk.dto.ResolutionLogRequest;
import com.servicedesk.dto.TicketResponseDto;
import com.servicedesk.exception.InvalidOperationException;
import com.servicedesk.exception.ResourceNotFoundException;
import com.servicedesk.repository.ResolutionLogRepository;
import com.servicedesk.repository.TicketRepository;
import com.servicedesk.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TroubleshootingService {

    private final TicketRepository ticketRepository;
    private final ResolutionLogRepository resolutionLogRepository;
    private final UserRepository userRepository;

    public TroubleshootingService(TicketRepository ticketRepository,
                                  ResolutionLogRepository resolutionLogRepository,
                                  UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.resolutionLogRepository = resolutionLogRepository;
        this.userRepository = userRepository;
    }

    public TicketResponseDto documentResolution(Long ticketId, ResolutionLogRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        User loggedBy = userRepository.findById(request.getLoggedById())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getLoggedById()));

        if (!loggedBy.canTroubleshoot()) {
            throw new InvalidOperationException("User role " + loggedBy.getRole() + " is not authorized to submit technical resolution logs.");
        }

        ResolutionLog log = new ResolutionLog(
                ticket,
                request.getProblemSummary(),
                request.getInvestigationSteps(),
                request.getRootCause(),
                request.getResolutionApplied(),
                loggedBy
        );

        ticket.attachResolutionLog(log);
        ticket.addHistory(loggedBy, "resolutionLog", null, "Attached Root Cause Analysis & Resolution");
        ticket.addHistory(loggedBy, "status", ticket.getStatus().name(), TicketStatus.RESOLVED.name());

        resolutionLogRepository.save(log);
        Ticket saved = ticketRepository.save(ticket);
        return TicketResponseDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public TicketResponseDto.ResolutionLogDto getResolutionLogByTicketId(Long ticketId) {
        ResolutionLog log = resolutionLogRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("No resolution log found for ticket ID: " + ticketId));

        TicketResponseDto.ResolutionLogDto dto = new TicketResponseDto.ResolutionLogDto();
        dto.setId(log.getId());
        dto.setProblemSummary(log.getProblemSummary());
        dto.setInvestigationSteps(log.getInvestigationSteps());
        dto.setRootCause(log.getRootCause());
        dto.setResolutionApplied(log.getResolutionApplied());
        dto.setLoggedAt(log.getLoggedAt());
        if (log.getLoggedBy() != null) {
            dto.setLoggedById(log.getLoggedBy().getId());
            dto.setLoggedByName(log.getLoggedBy().getName());
        }
        return dto;
    }
}
