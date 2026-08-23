package com.servicedesk.service;

import com.servicedesk.domain.*;
import com.servicedesk.domain.enums.*;
import com.servicedesk.dto.ConvertToBugRequest;
import com.servicedesk.dto.TicketResponseDto;
import com.servicedesk.exception.InvalidOperationException;
import com.servicedesk.repository.BugReportRepository;
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
class BugReportServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private BugReportRepository bugReportRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BugReportService bugReportService;

    private Developer developer;
    private Employee employee;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        developer = new Developer("David Chen", "david@company.com", "Engineering", "Backend Core", "Java, Spring");
        developer.setId(5L);

        employee = new Employee("Alice", "alice@company.com", "Finance", "Analyst", "Floor 3");
        employee.setId(1L);

        ticket = new Ticket("TICK-1024", "Login 500 Error", "Password reset causes 500 error",
                TicketPriority.HIGH, TicketCategory.APPLICATION_BUG, employee);
        ticket.setId(100L);
    }

    @Test
    @DisplayName("Convert Ticket to Bug - Success with QA reproduction steps")
    void testConvertTicketToBug_Success() {
        ConvertToBugRequest request = new ConvertToBugRequest(
                "NPE in AuthTokenService on password reset",
                BugSeverity.CRITICAL,
                "Production",
                "1. Reset password 2. Login",
                "Successful login",
                "HTTP 500",
                "NullPointerException at TokenService.java:84",
                5L
        );

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(5L)).thenReturn(Optional.of(developer));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        TicketResponseDto response = bugReportService.convertTicketToBug(100L, request);

        assertThat(ticket.getBugReport()).isNotNull();
        assertThat(ticket.getBugReport().getSeverity()).isEqualTo(BugSeverity.CRITICAL);
        assertThat(ticket.getBugReport().getAssignedDeveloper()).isEqualTo(developer);
        verify(bugReportRepository, times(1)).save(any(BugReport.class));
    }

    @Test
    @DisplayName("Convert Ticket to Bug - Throws Exception if already converted")
    void testConvertTicketToBug_AlreadyConverted() {
        BugReport existingBug = new BugReport("BUG-301", ticket, "Old Bug", BugSeverity.TRIVIAL, "QA", "steps", "exp", "act", null, developer);
        ticket.attachBugReport(existingBug);

        ConvertToBugRequest request = new ConvertToBugRequest(
                "Another bug", BugSeverity.MAJOR, "QA", "steps", "exp", "act", null, 5L
        );

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> bugReportService.convertTicketToBug(100L, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("already linked to bug");
    }
}
