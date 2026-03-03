package com.busbooking.bus_booking_system.dto.response;

public record CircuitResponseDTO(
        Long id,
        String name,
        String slug,
        String description,
        String iconUrl,
        String bannerImageUrl
) {}
