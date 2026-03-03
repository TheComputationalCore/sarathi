package com.busbooking.bus_booking_system.controller;

import com.busbooking.bus_booking_system.dto.response.ThemeResponseDTO;
import com.busbooking.bus_booking_system.service.ThemeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/themes")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    /**
     * Public endpoint
     * GET /api/themes
     */
    @GetMapping
    public ResponseEntity<List<ThemeResponseDTO>> getAllThemes() {
        return ResponseEntity.ok(
                themeService.getAllThemes()
        );
    }
}