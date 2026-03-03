package com.busbooking.bus_booking_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ThemeResponseDTO {

    private Long id;
    private String name;
    private String description;
    private String icon;
    private Integer priorityWeight;
}