package com.busbooking.bus_booking_system.controller;

import com.busbooking.bus_booking_system.dto.response.YatraPointWithBusesDTO;
import com.busbooking.bus_booking_system.service.YatraPointService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/yatra-points")
public class YatraPointController {

    private final YatraPointService yatraPointService;

    public YatraPointController(YatraPointService yatraPointService) {
        this.yatraPointService = yatraPointService;
    }

    /**
     * GET /api/yatra-points
     * GET /api/yatra-points?circuitId=1
     */
    @GetMapping
    public ResponseEntity<List<YatraPointWithBusesDTO>> getYatraPoints(
            @RequestParam(required = false) Long circuitId
    ) {
        return ResponseEntity.ok(
                yatraPointService.getYatraPoints(circuitId)
        );
    }
}
