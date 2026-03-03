package com.busbooking.bus_booking_system.controller;

import com.busbooking.bus_booking_system.dto.RouteMapDTO;
import com.busbooking.bus_booking_system.service.RouteMapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/buses")
public class RouteMapController {

    private final RouteMapService routeMapService;

    public RouteMapController(RouteMapService routeMapService) {
        this.routeMapService = routeMapService;
    }

    @GetMapping("/{id}/map")
    public ResponseEntity<RouteMapDTO> getRouteMap(@PathVariable Long id) {
        return ResponseEntity.ok(routeMapService.getRouteMap(id));
    }
}
