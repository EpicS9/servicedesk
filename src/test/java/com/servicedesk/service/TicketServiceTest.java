package com.servicedesk.service;

import com.servicedesk.domain.*;
import com.servicedesk.domain.enums.*;
import com.servicedesk.dto.*;
import com.servicedesk.exception.InvalidOperationException;
import com.servicedesk.exception.ResourceNotFoundException;
import com.servicedesk.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketCommentRepository commentRepository;

    @Mock
    private TicketHistoryRepository historyRepository;

    @InjectMocks
    private TicketService ticketService;

    private Employee employee;
    private SupportEngineer supportEngineer;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        employee = new Employee("Alice Smith", "alice@company.com", "Finance", "Analyst", "Floor 3");
        employee.setId(1L);

        supportEngineer = new SupportEngineer("Bob Tech", "bob@servicedesk.internal", "Support", "Tier 2", "Network");
        supportEngineer.setId(2L);

        ticket = new Ticket("TICK-1001", "VPN disconnected", "Unable to connect to VPN",
                TicketPriority.HIGH, TicketCategory.NETWORK, employee);
        ticket.setId(100L);
    }

    @Test
    @DisplayName("Create Ticket - Success with valid data")
    void testCreateTicket_Success() {
        CreateTicketRequest request = new CreateTicketRequest(
                "VPN disconnected",
                "Unable to connect to VPN",
                TicketPriority.HIGH,
                TicketCategory.NETWORK,
                1L
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            t.setId(101L);
            return t;
        });

        TicketResponseDto response = ticketService.createTicket(request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("VPN disconnected");
        assertThat(response.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(response.getCreatorName()).isEqualTo("Alice Smith");
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Create Ticket - Throws Exception when Creator Not Found")
    void testCreateTicket_CreatorNotFound() {
        CreateTicketRequest request = new CreateTicketRequest(
                "VPN disconnected",
                "Unable to connect to VPN",
                TicketPriority.HIGH,
                TicketCategory.NETWORK,
                999L
        );

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.createTicket(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Creator user not found");
    }

    @Test
    @DisplayName("Assign Ticket - Updates assignedTo and transitions state to IN_PROGRESS")
    void testAssignTicket_Success() {
        AssignTicketRequest request = new AssignTicketRequest(2L, 2L);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(supportEngineer));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        TicketResponseDto response = ticketService.assignTicket(100L, request);

        assertThat(ticket.getAssignedTo()).isEqualTo(supportEngineer);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        verify(ticketRepository, times(1)).save(ticket);
    }

    @Test
    @DisplayName("Assign Ticket - Throws Exception when assigning to regular employee")
    void testAssignTicket_InvalidRole() {
        Employee anotherEmp = new Employee("Charlie", "charlie@company.com", "Marketing", "Specialist", "Floor 1");
        anotherEmp.setId(3L);

        AssignTicketRequest request = new AssignTicketRequest(3L, 2L);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(supportEngineer));
        when(userRepository.findById(3L)).thenReturn(Optional.of(anotherEmp));

        assertThatThrownBy(() -> ticketService.assignTicket(100L, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("cannot be assigned support tickets");
    }

    @Test
    @DisplayName("Update Status - Employee cannot close other employee's ticket")
    void testUpdateStatus_EmployeeClosingOthersTicket_Denied() {
        Employee intruder = new Employee("Intruder", "intruder@company.com", "Sales", "Rep", "Floor 2");
        intruder.setId(99L);

        UpdateStatusRequest request = new UpdateStatusRequest(TicketStatus.CLOSED, 99L);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(99L)).thenReturn(Optional.of(intruder));

        assertThatThrownBy(() -> ticketService.updateStatus(100L, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Employees can only close their own support tickets");
    }

    @Test
    @DisplayName("Add Comment - Attaches comment and creates history entry")
    void testAddComment_Success() {
        CreateCommentRequest request = new CreateCommentRequest(2L, "Investigating connection logs", false);

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(supportEngineer));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        TicketResponseDto response = ticketService.addComment(100L, request);

        assertThat(ticket.getComments()).hasSize(1);
        assertThat(ticket.getComments().get(0).getMessage()).isEqualTo("Investigating connection logs");
        verify(commentRepository, times(1)).save(any(TicketComment.class));
    }
}
