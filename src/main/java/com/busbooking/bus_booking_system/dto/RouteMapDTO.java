package com.busbooking.bus_booking_system.dto;

import java.io.Serializable;
import java.util.List;

public record RouteMapDTO(

        Long busId,
        String fromLocation,
        String toLocation,
        String circuitName,
        List<Point> points

) implements Serializable {

    public record Point(
            Long id,
            String name,
            String slug,
            Double latitude,
            Double longitude,
            String imageUrl,
            String shortHistory,
            Integer recommendedZoomLevel
    ) implements Serializable {}
}
