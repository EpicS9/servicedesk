package com.servicedesk.controller;

import com.servicedesk.dto.ResolutionLogRequest;
import com.servicedesk.dto.TicketResponseDto;
import com.servicedesk.service.TroubleshootingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets/{ticketId}/troubleshooting")
@CrossOrigin(origins = "*")
public class TroubleshootingController {

    private final TroubleshootingService troubleshootingService;

    public TroubleshootingController(TroubleshootingService troubleshootingService) {
        this.troubleshootingService = troubleshootingService;
    }

    @PostMapping
    public ResponseEntity<TicketResponseDto> documentResolution(
            @PathVariable Long ticketId,
            @Valid @RequestBody ResolutionLogRequest request) {
        TicketResponseDto updated = troubleshootingService.documentResolution(ticketId, request);
        return new ResponseEntity<>(updated, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<TicketResponseDto.ResolutionLogDto> getResolutionLog(@PathVariable Long ticketId) {
        TicketResponseDto.ResolutionLogDto logDto = troubleshootingService.getResolutionLogByTicketId(ticketId);
        return ResponseEntity.ok(logDto);
    }
}
