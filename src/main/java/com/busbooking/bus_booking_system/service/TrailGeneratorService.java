package com.busbooking.bus_booking_system.service;

import com.busbooking.bus_booking_system.dto.response.TrailResponseDTO;
import com.busbooking.bus_booking_system.entity.Era;
import com.busbooking.bus_booking_system.entity.Theme;
import com.busbooking.bus_booking_system.repository.EraRepository;
import com.busbooking.bus_booking_system.repository.ThemeRepository;
import com.busbooking.bus_booking_system.service.trail.TrailEngine;
import com.busbooking.bus_booking_system.service.trail.TrailMode;
import com.busbooking.bus_booking_system.service.trail.cache.TrailCacheKeyBuilder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrailGeneratorService {

    private static final int DEFAULT_LIMIT = 6;
    private static final int MAX_LIMIT = 15;

    private final ThemeRepository themeRepository;
    private final EraRepository eraRepository;
    private final TrailEngine trailEngine;
    private final TrailCacheKeyBuilder cacheKeyBuilder;

    private final Counter trailGeneratedCounter;
    private final Timer trailGenerationTimer;

    public TrailGeneratorService(ThemeRepository themeRepository,
                                 EraRepository eraRepository,
                                 TrailEngine trailEngine,
                                 TrailCacheKeyBuilder cacheKeyBuilder,
                                 MeterRegistry meterRegistry) {

        this.themeRepository = themeRepository;
        this.eraRepository = eraRepository;
        this.trailEngine = trailEngine;
        this.cacheKeyBuilder = cacheKeyBuilder;

        this.trailGeneratedCounter =
                meterRegistry.counter("sarathi.trails.generated.total");

        this.trailGenerationTimer =
                meterRegistry.timer("sarathi.trails.generation.duration");
    }

    @Cacheable(
            value = "generated_trails",
            key = "#root.target.buildCacheKey(#themeNames, #eraNames, #maxStops, #mode)"
    )
    public TrailResponseDTO generateTrail(
            List<String> themeNames,
            List<String> eraNames,
            Integer maxStops,
            TrailMode mode
    ) {

        return trailGenerationTimer.record(() -> {

            trailGeneratedCounter.increment();

            if (themeNames == null || themeNames.isEmpty()) {
                throw new IllegalArgumentException("At least one theme must be selected.");
            }

            // ==========================
            // Normalize Themes
            // ==========================

            List<String> normalizedThemes = themeNames.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            if (normalizedThemes.isEmpty()) {
                throw new IllegalArgumentException("Invalid theme values provided.");
            }

            // ==========================
            // Normalize Eras (optional)
            // ==========================

            List<String> normalizedEras = eraNames == null
                    ? Collections.emptyList()
                    : eraNames.stream()
                        .filter(StringUtils::hasText)
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());

            // ==========================
            // Safe Stop Limit
            // ==========================

            int limit = (maxStops == null || maxStops <= 0)
                    ? DEFAULT_LIMIT
                    : Math.min(maxStops, MAX_LIMIT);

            // ==========================
            // Fetch Themes
            // ==========================

            List<Theme> selectedThemes =
                    themeRepository.findAll().stream()
                            .filter(t ->
                                    normalizedThemes.contains(
                                            t.getName().trim().toLowerCase()
                                    )
                            )
                            .collect(Collectors.toList());

            if (selectedThemes.isEmpty()) {
                throw new IllegalArgumentException("No valid themes found.");
            }

            Set<Long> selectedThemeIds =
                    selectedThemes.stream()
                            .map(Theme::getId)
                            .collect(Collectors.toSet());

            // ==========================
            // Fetch Eras (optional)
            // ==========================

            List<Era> selectedEras = normalizedEras.isEmpty()
                    ? Collections.emptyList()
                    : eraRepository.findAll().stream()
                        .filter(e ->
                                normalizedEras.contains(
                                        e.getName().trim().toLowerCase()
                                )
                        )
                        .collect(Collectors.toList());

            Set<Long> selectedEraIds =
                    selectedEras.stream()
                            .map(Era::getId)
                            .collect(Collectors.toSet());

            // ==========================
            // Default Mode
            // ==========================

            TrailMode finalMode = (mode == null)
                    ? TrailMode.THEMATIC
                    : mode;

            // ==========================
            // Delegate to Engine
            // ==========================

            return trailEngine.buildTrail(
                    selectedThemes,
                    selectedThemeIds,
                    selectedEraIds,      // ✅ ERA FILTER NOW ACTIVE
                    normalizedThemes,
                    limit,
                    finalMode
            );
        });
    }

    public String buildCacheKey(
            List<String> themeNames,
            List<String> eraNames,
            Integer maxStops,
            TrailMode mode
    ) {

        List<String> normalizedThemes =
                themeNames == null
                        ? Collections.emptyList()
                        : themeNames.stream()
                            .filter(StringUtils::hasText)
                            .map(String::trim)
                            .map(String::toLowerCase)
                            .distinct()
                            .sorted()
                            .collect(Collectors.toList());

        List<String> normalizedEras =
                eraNames == null
                        ? Collections.emptyList()
                        : eraNames.stream()
                            .filter(StringUtils::hasText)
                            .map(String::trim)
                            .map(String::toLowerCase)
                            .distinct()
                            .sorted()
                            .collect(Collectors.toList());

        return cacheKeyBuilder.build(
                normalizedThemes,
                normalizedEras,
                maxStops,
                mode == null ? TrailMode.THEMATIC.name() : mode.name()
        );
    }
}