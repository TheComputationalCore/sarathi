package com.busbooking.bus_booking_system.service;

import com.busbooking.bus_booking_system.dto.response.ThemeResponseDTO;
import com.busbooking.bus_booking_system.entity.Theme;
import com.busbooking.bus_booking_system.repository.ThemeRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<ThemeResponseDTO> getAllThemes() {

        return themeRepository.findAll().stream()
                .sorted(
                        Comparator
                                .comparing(
                                        (Theme t) -> 
                                            t.getPriorityWeight() != null
                                                ? t.getPriorityWeight()
                                                : 0
                                )
                                .reversed()
                                .thenComparing(Theme::getName)
                )
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ThemeResponseDTO mapToDTO(Theme theme) {
        return ThemeResponseDTO.builder()
                .id(theme.getId())
                .name(theme.getName())
                .description(theme.getDescription())
                .icon(theme.getIcon())
                .priorityWeight(
                        theme.getPriorityWeight() != null
                                ? theme.getPriorityWeight()
                                : 0
                )
                .build();
    }
}