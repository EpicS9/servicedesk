package com.servicedesk.service;

import com.servicedesk.domain.*;
import com.servicedesk.domain.enums.*;
import com.servicedesk.dto.*;
import com.servicedesk.exception.InvalidOperationException;
import com.servicedesk.repository.ResolutionLogRepository;
import com.servicedesk.repository.TicketRepository;
import com.servicedesk.repository.UserRepository;
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
class TroubleshootingServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ResolutionLogRepository resolutionLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TroubleshootingService troubleshootingService;

    private SupportEngineer supportEngineer;
    private Employee employee;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        supportEngineer = new SupportEngineer("Alex Rivera", "alex@servicedesk.internal", "Support", "Tier 2", "Database");
        supportEngineer.setId(2L);

        employee = new Employee("Alice", "alice@company.com", "Finance", "Analyst", "Floor 3");
        employee.setId(1L);

        ticket = new Ticket("TICK-1018", "Database timeout", "Reporting DB query timed out",
                TicketPriority.CRITICAL, TicketCategory.DATABASE, employee);
        ticket.setId(100L);
    }

    @Test
    @DisplayName("Document Resolution - Successfully records root cause and transitions status to RESOLVED")
    void testDocumentResolution_Success() {
        ResolutionLogRequest request = new ResolutionLogRequest(
                "HikariCP pool exhausted",
                "Checked application logs, found connection leak in report batch",
                "Unclosed Hibernate session",
                "Restarted service, increased pool size to 50, hotfixed leak",
                2L
        );

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(supportEngineer));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        TicketResponseDto response = troubleshootingService.documentResolution(100L, request);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.RESOLVED);
        assertThat(ticket.getResolutionLog()).isNotNull();
        assertThat(ticket.getResolutionLog().getRootCause()).isEqualTo("Unclosed Hibernate session");
        verify(resolutionLogRepository, times(1)).save(any(ResolutionLog.class));
        verify(ticketRepository, times(1)).save(ticket);
    }

    @Test
    @DisplayName("Document Resolution - Throws Exception if non-technical employee attempts to submit resolution")
    void testDocumentResolution_UnauthorizedRole() {
        ResolutionLogRequest request = new ResolutionLogRequest(
                "HikariCP pool exhausted",
                "Checked logs",
                "Root cause",
                "Restarted",
                1L
        );

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> troubleshootingService.documentResolution(100L, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("not authorized to submit technical resolution logs");
    }
}
