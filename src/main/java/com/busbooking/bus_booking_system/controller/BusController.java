package com.busbooking.bus_booking_system.controller;

import com.busbooking.bus_booking_system.dto.response.BusResponseDTO;
import com.busbooking.bus_booking_system.dto.RouteMapDTO;
import com.busbooking.bus_booking_system.service.BusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buses")
public class BusController {

    private final BusService busService;

    public BusController(BusService busService) {
        this.busService = busService;
    }

    // =========================================================
    // PUBLIC SEARCH ENDPOINT
    // =========================================================

    /**
     * Supported:
     *  - /api/buses
     *  - /api/buses?from=Delhi&to=Varanasi
     *  - /api/buses?circuitId=1
     *  - /api/buses?circuitSlug=knowledge-corridor
     *  - /api/buses?circuitId=1&from=Delhi&to=Varanasi
     */
    @GetMapping
    public ResponseEntity<List<BusResponseDTO>> getBuses(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Long circuitId,
            @RequestParam(required = false) String circuitSlug,
            @RequestParam(required = false) String themes
    ) {

        if (from != null) from = from.trim();
        if (to != null) to = to.trim();
        if (circuitSlug != null) circuitSlug = circuitSlug.trim();
        if (themes != null) themes = themes.trim();

        // If only one of from/to is provided → return empty list (not 400)
        if ((from != null && to == null) ||
            (from == null && to != null)) {

            return ResponseEntity.ok(List.of());
        }

        List<BusResponseDTO> result =
                busService.findBuses(from, to, circuitId, circuitSlug, themes);

        return ResponseEntity.ok(result);
    }

    // =========================================================
    // FETCH SINGLE BUS
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<BusResponseDTO> getBusById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                busService.findById(id)
        );
    }

    // =========================================================
    // MAP ROUTE ENDPOINT
    // =========================================================

    @GetMapping("/{id}/route-map")
    public ResponseEntity<RouteMapDTO> getRouteMap(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                busService.getRouteMap(id)
        );
    }
}
