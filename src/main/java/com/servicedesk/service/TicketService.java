package com.servicedesk.service;

import com.servicedesk.domain.*;
import com.servicedesk.domain.enums.Role;
import com.servicedesk.domain.enums.TicketPriority;
import com.servicedesk.domain.enums.TicketStatus;
import com.servicedesk.dto.*;
import com.servicedesk.exception.InvalidOperationException;
import com.servicedesk.exception.ResourceNotFoundException;
import com.servicedesk.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketCommentRepository commentRepository;
    private final TicketHistoryRepository historyRepository;

    private static final AtomicLong TICKET_SEQUENCE = new AtomicLong(1000);

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository,
                         TicketCommentRepository commentRepository, TicketHistoryRepository historyRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.historyRepository = historyRepository;
    }

    public TicketResponseDto createTicket(CreateTicketRequest request) {
        User creator = userRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Creator user not found with ID: " + request.getCreatorId()));

        String ticketNumber = "SD-" + TICKET_SEQUENCE.incrementAndGet();
        Ticket ticket = new Ticket(ticketNumber, request.getTitle(), request.getDescription(),
                request.getPriority(), request.getCategory(), creator);

        ticket.addHistory(creator, "status", null, TicketStatus.OPEN.name());
        Ticket saved = ticketRepository.save(ticket);
        return TicketResponseDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<TicketResponseDto> getAllTickets(TicketStatus status, TicketPriority priority, String query) {
        List<Ticket> tickets = ticketRepository.findAll();
        
        if (query != null && !query.trim().isEmpty()) {
            String q = query.trim().toLowerCase();
            tickets = tickets.stream()
                    .filter(t -> (t.getTitle() != null && t.getTitle().toLowerCase().contains(q)) ||
                                 (t.getDescription() != null && t.getDescription().toLowerCase().contains(q)) ||
                                 (t.getTicketNumber() != null && t.getTicketNumber().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }
        
        if (status != null) {
            tickets = tickets.stream().filter(t -> t.getStatus() == status).collect(Collectors.toList());
        }
        
        if (priority != null) {
            tickets = tickets.stream().filter(t -> t.getPriority() == priority).collect(Collectors.toList());
        }
        
        return tickets.stream().map(TicketResponseDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketResponseDto getTicketById(Long id) {
        Ticket ticket = findTicketEntity(id);
        return TicketResponseDto.fromEntity(ticket);
    }

    public TicketResponseDto updateStatus(Long ticketId, UpdateStatusRequest request) {
        Ticket ticket = findTicketEntity(ticketId);
        User actionUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        if (request.getStatus() == TicketStatus.CLOSED && actionUser.getRole() == Role.EMPLOYEE) {
            if (!ticket.getCreator().getId().equals(actionUser.getId())) {
                throw new InvalidOperationException("Employees can only close their own support tickets.");
            }
        }

        if (request.getStatus() == TicketStatus.RESOLVED && ticket.getResolutionLog() == null) {
            ticket.addHistory(actionUser, "status", ticket.getStatus().name(), TicketStatus.RESOLVED.name());
        }

        ticket.updateStatus(request.getStatus(), actionUser);
        Ticket saved = ticketRepository.save(ticket);
        return TicketResponseDto.fromEntity(saved);
    }

    public TicketResponseDto assignTicket(Long ticketId, AssignTicketRequest request) {
        Ticket ticket = findTicketEntity(ticketId);
        User actionUser = userRepository.findById(request.getActionUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Action user not found with ID: " + request.getActionUserId()));

        User targetUser = null;
        if (request.getAssignedToUserId() != null) {
            targetUser = userRepository.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee user not found with ID: " + request.getAssignedToUserId()));

            if (!targetUser.canBeAssignedTickets()) {
                throw new InvalidOperationException("User role " + targetUser.getRole() + " cannot be assigned support tickets.");
            }
        }

        ticket.assignTo(targetUser, actionUser);
        Ticket saved = ticketRepository.save(ticket);
        return TicketResponseDto.fromEntity(saved);
    }

    public TicketResponseDto addComment(Long ticketId, CreateCommentRequest request) {
        Ticket ticket = findTicketEntity(ticketId);
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author user not found with ID: " + request.getAuthorId()));

        TicketComment comment = new TicketComment(ticket, author, request.getMessage(), request.isInternal());
        ticket.addComment(comment);
        commentRepository.save(comment);

        ticket.addHistory(author, "comment", null, "Added comment (" + (request.isInternal() ? "Internal" : "Public") + ")");
        Ticket saved = ticketRepository.save(ticket);
        return TicketResponseDto.fromEntity(saved);
    }

    public void deleteTicket(Long id) {
        Ticket ticket = findTicketEntity(id);
        ticketRepository.delete(ticket);
    }

    public Ticket findTicketEntity(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + id));
    }
}
