package com.servicedesk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicedesk.domain.Employee;
import com.servicedesk.domain.Ticket;
import com.servicedesk.domain.enums.TicketCategory;
import com.servicedesk.domain.enums.TicketPriority;
import com.servicedesk.domain.enums.TicketStatus;
import com.servicedesk.dto.CreateTicketRequest;
import com.servicedesk.dto.TicketResponseDto;
import com.servicedesk.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TicketService ticketService;

    @Test
    @DisplayName("GET /api/tickets - Returns 200 OK and JSON array")
    void testGetAllTickets() throws Exception {
        Employee emp = new Employee("Alice", "alice@company.com", "Finance", "Analyst", "Floor 3");
        emp.setId(1L);
        Ticket ticket = new Ticket("TICK-1001", "Printer offline", "Floor 3 printer offline",
                TicketPriority.LOW, TicketCategory.HARDWARE, emp);
        ticket.setId(10L);

        when(ticketService.getAllTickets(null, null, null))
                .thenReturn(Collections.singletonList(TicketResponseDto.fromEntity(ticket)));

        mockMvc.perform(get("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticketNumber").value("TICK-1001"))
                .andExpect(jsonPath("$[0].title").value("Printer offline"))
                .andExpect(jsonPath("$[0].priority").value("LOW"));
    }

    @Test
    @DisplayName("POST /api/tickets - Success returns 201 Created")
    void testCreateTicket_Valid() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest(
                "VPN disconnected",
                "Unable to connect to VPN from home network",
                TicketPriority.HIGH,
                TicketCategory.NETWORK,
                1L
        );

        Employee emp = new Employee("Alice", "alice@company.com", "Finance", "Analyst", "Floor 3");
        emp.setId(1L);
        Ticket ticket = new Ticket("TICK-1002", request.getTitle(), request.getDescription(),
                request.getPriority(), request.getCategory(), emp);
        ticket.setId(11L);

        when(ticketService.createTicket(any(CreateTicketRequest.class)))
                .thenReturn(TicketResponseDto.fromEntity(ticket));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketNumber").value("TICK-1002"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("POST /api/tickets - Validation Failure returns 400 Bad Request")
    void testCreateTicket_ValidationFailure() throws Exception {
        CreateTicketRequest invalidRequest = new CreateTicketRequest(
                "", // Blank title -> triggers @NotBlank validation
                "Short", // < 10 chars -> triggers @Size validation
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }
}
