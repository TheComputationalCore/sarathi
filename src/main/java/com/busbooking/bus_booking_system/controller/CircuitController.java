package com.busbooking.bus_booking_system.controller;

import com.busbooking.bus_booking_system.dto.response.CircuitResponseDTO;
import com.busbooking.bus_booking_system.service.CircuitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/circuits")
public class CircuitController {

    private final CircuitService circuitService;

    public CircuitController(CircuitService circuitService) {
        this.circuitService = circuitService;
    }

    /**
     * Public endpoint
     * GET /api/circuits
     */
    @GetMapping
    public ResponseEntity<List<CircuitResponseDTO>> getActiveCircuits() {

        return ResponseEntity.ok(
                circuitService.getActiveCircuits()
        );
    }

    /**
     * GET /api/circuits/{slug}
     */
    @GetMapping("/{slug}")
    public ResponseEntity<CircuitResponseDTO> getBySlug(
            @PathVariable String slug
    ) {

        return ResponseEntity.ok(
                circuitService.getBySlug(slug)
        );
    }
}
