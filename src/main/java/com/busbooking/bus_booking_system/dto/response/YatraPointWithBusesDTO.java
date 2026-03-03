package com.busbooking.bus_booking_system.dto.response;

import java.util.List;

public record YatraPointWithBusesDTO(
        Long id,
        String name,
        String slug,
        String shortHistory,
        Double latitude,
        Double longitude,
        String imageUrl,
        List<BusResponseDTO> buses
) {}
