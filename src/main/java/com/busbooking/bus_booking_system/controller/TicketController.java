package com.busbooking.bus_booking_system.controller;

import com.busbooking.bus_booking_system.service.TicketService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<byte[]> downloadTicket(@PathVariable Long bookingId,
                                                 Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        byte[] pdf = ticketService.generateTicket(
                bookingId,
                authentication.getName(),
                isAdmin
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ticket_" + bookingId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
