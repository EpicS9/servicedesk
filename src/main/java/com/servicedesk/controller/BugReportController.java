package com.servicedesk.controller;

import com.servicedesk.domain.enums.BugStatus;
import com.servicedesk.dto.ConvertToBugRequest;
import com.servicedesk.dto.TicketResponseDto;
import com.servicedesk.service.BugReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bugs")
@CrossOrigin(origins = "*")
public class BugReportController {

    private final BugReportService bugReportService;

    public BugReportController(BugReportService bugReportService) {
        this.bugReportService = bugReportService;
    }

    @GetMapping
    public ResponseEntity<List<TicketResponseDto.BugReportDto>> getAllBugs() {
        return ResponseEntity.ok(bugReportService.getAllBugs());
    }

    @PostMapping("/tickets/{ticketId}/convert")
    public ResponseEntity<TicketResponseDto> convertTicketToBug(
            @PathVariable Long ticketId,
            @Valid @RequestBody ConvertToBugRequest request) {
        TicketResponseDto updated = bugReportService.convertTicketToBug(ticketId, request);
        return new ResponseEntity<>(updated, HttpStatus.CREATED);
    }

    @PutMapping("/{bugId}/status")
    public ResponseEntity<TicketResponseDto.BugReportDto> updateBugStatus(
            @PathVariable Long bugId,
            @RequestParam BugStatus status) {
        TicketResponseDto.BugReportDto updated = bugReportService.updateBugStatus(bugId, status);
        return ResponseEntity.ok(updated);
    }
}
