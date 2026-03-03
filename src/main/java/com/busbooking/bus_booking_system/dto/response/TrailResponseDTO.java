package com.busbooking.bus_booking_system.dto.response;

import java.io.Serializable;
import java.util.List;

public record TrailResponseDTO(

        String trailName,

        List<String> themes,

        List<TrailStop> stops,

        String narrativeSummary

) implements Serializable {

    public record TrailStop(
            Long id,
            String name,
            String slug,
            Double latitude,
            Double longitude,
            String imageUrl,
            String shortHistory
    ) implements Serializable {}
}
