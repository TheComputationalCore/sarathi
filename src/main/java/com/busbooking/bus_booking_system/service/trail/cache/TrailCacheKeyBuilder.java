package com.busbooking.bus_booking_system.service.trail.cache;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class TrailCacheKeyBuilder {

    public String build(
            List<String> themeNames,
            List<String> eraNames,
            Integer maxStops,
            String mode
    ) {

        List<String> normalizedThemes =
                themeNames == null
                        ? List.of()
                        : themeNames.stream()
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .map(String::toLowerCase)
                            .sorted()
                            .collect(Collectors.toList());

        List<String> normalizedEras =
                eraNames == null
                        ? List.of()
                        : eraNames.stream()
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .map(String::toLowerCase)
                            .sorted()
                            .collect(Collectors.toList());

        String themeKey = normalizedThemes.isEmpty()
                ? "no-themes"
                : String.join("-", normalizedThemes);

        String eraKey = normalizedEras.isEmpty()
                ? "no-eras"
                : String.join("-", normalizedEras);

        String limitKey = maxStops == null
                ? "default"
                : maxStops.toString();

        String modeKey = mode == null
                ? "THEMATIC"
                : mode;

        return String.format(
                "trail::themes=%s::eras=%s::limit=%s::mode=%s",
                themeKey,
                eraKey,
                limitKey,
                modeKey
        );
    }
}