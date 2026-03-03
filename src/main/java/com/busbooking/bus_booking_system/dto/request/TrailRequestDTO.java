package com.busbooking.bus_booking_system.dto.request;

import com.busbooking.bus_booking_system.service.trail.TrailMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TrailRequestDTO(

        @NotEmpty(message = "At least one theme must be selected")
        @Size(max = 5, message = "Maximum 5 themes allowed")
        List<String> themes,

        @Size(max = 5, message = "Maximum 5 eras allowed")
        List<String> eras,

        Integer maxStops,

        TrailMode mode   // Optional: THEMATIC (default) or CHRONOLOGICAL
) {}