package com.busbooking.bus_booking_system.controller;

import com.busbooking.bus_booking_system.dto.common.ApiResponse;
import com.busbooking.bus_booking_system.dto.request.TrailRequestDTO;
import com.busbooking.bus_booking_system.dto.response.TrailResponseDTO;
import com.busbooking.bus_booking_system.service.TrailGeneratorService;
import com.busbooking.bus_booking_system.service.trail.TrailMode;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trails")
public class TrailController {

    private final TrailGeneratorService trailGeneratorService;

    public TrailController(TrailGeneratorService trailGeneratorService) {
        this.trailGeneratorService = trailGeneratorService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<TrailResponseDTO>> generateTrail(
            @Valid @RequestBody TrailRequestDTO request
    ) {

        TrailMode mode = request.mode() == null
                ? TrailMode.THEMATIC
                : request.mode();

        TrailResponseDTO response =
                trailGeneratorService.generateTrail(
                        request.themes(),
                        request.eras(),       // ✅ now passing eras
                        request.maxStops(),
                        mode
                );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}