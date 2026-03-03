package com.busbooking.bus_booking_system.service.trail;

import com.busbooking.bus_booking_system.dto.response.TrailResponseDTO;
import com.busbooking.bus_booking_system.entity.Era;
import com.busbooking.bus_booking_system.entity.Theme;
import com.busbooking.bus_booking_system.entity.YatraPoint;
import com.busbooking.bus_booking_system.repository.YatraPointRepository;
import com.busbooking.bus_booking_system.service.trail.narrative.NarrativeComposer;
import com.busbooking.bus_booking_system.service.trail.optimizer.GeographicOptimizer;
import com.busbooking.bus_booking_system.service.trail.strategy.TrailScoringStrategy;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TrailEngine {

    private final YatraPointRepository yatraPointRepository;
    private final TrailScoringStrategy scoringStrategy;
    private final GeographicOptimizer geographicOptimizer;
    private final NarrativeComposer narrativeComposer;

    public TrailEngine(YatraPointRepository yatraPointRepository,
                       TrailScoringStrategy scoringStrategy,
                       GeographicOptimizer geographicOptimizer,
                       NarrativeComposer narrativeComposer) {
        this.yatraPointRepository = yatraPointRepository;
        this.scoringStrategy = scoringStrategy;
        this.geographicOptimizer = geographicOptimizer;
        this.narrativeComposer = narrativeComposer;
    }

    public TrailResponseDTO buildTrail(
            List<Theme> selectedThemes,
            Set<Long> selectedThemeIds,
            Set<Long> selectedEraIds,      // ✅ NEW
            List<String> themeNames,
            int limit,
            TrailMode mode
    ) {

        // ==========================
        // Fetch Active Points
        // ==========================

        List<YatraPoint> points =
                yatraPointRepository.findActiveWithThemesAndEras();

        // ==========================
        // ERA FILTER (if provided)
        // ==========================

        if (selectedEraIds != null && !selectedEraIds.isEmpty()) {

            points = points.stream()
                    .filter(point ->
                            point.getEras() != null &&
                            point.getEras().stream()
                                    .anyMatch(era -> selectedEraIds.contains(era.getId()))
                    )
                    .collect(Collectors.toList());
        }

        if (points.isEmpty()) {
            return new TrailResponseDTO(
                    "Civilizational Path",
                    themeNames,
                    List.of(),
                    "No heritage nodes matched your selected filters."
            );
        }

        // ==========================
        // Score by Themes
        // ==========================

        List<TrailScoringStrategy.ScoredPoint> scored =
                scoringStrategy.score(points, selectedThemeIds);

        if (scored.isEmpty()) {
            return new TrailResponseDTO(
                    "Civilizational Path",
                    themeNames,
                    List.of(),
                    "No heritage nodes matched your selected themes."
            );
        }

        List<YatraPoint> rankedPoints =
                scored.stream()
                        .map(TrailScoringStrategy.ScoredPoint::point)
                        .collect(Collectors.toList());

        // ==========================
        // CHRONOLOGICAL MODE
        // ==========================

        if (mode == TrailMode.CHRONOLOGICAL) {

            rankedPoints.sort(
                    Comparator
                            .comparingInt(this::getEarliestEraOrder)
                            .thenComparing(YatraPoint::getName)
            );
        }

        // ==========================
        // GEOGRAPHIC OPTIMIZATION
        // ==========================

        List<YatraPoint> optimized =
                geographicOptimizer.optimize(rankedPoints, limit);

        List<YatraPoint> limited =
                optimized.stream()
                        .limit(limit)
                        .collect(Collectors.toList());

        return new TrailResponseDTO(
                narrativeComposer.buildTrailName(selectedThemes, limited),
                themeNames,
                limited.stream()
                        .map(point -> new TrailResponseDTO.TrailStop(
                                point.getId(),
                                point.getName(),
                                point.getSlug(),
                                point.getLatitude(),
                                point.getLongitude(),
                                point.getImageUrl(),
                                point.getShortHistory()
                        ))
                        .collect(Collectors.toList()),
                narrativeComposer.buildNarrativeSummary(selectedThemes, limited)
        );
    }

    private int getEarliestEraOrder(YatraPoint point) {

        if (point.getEras() == null || point.getEras().isEmpty()) {
            return Integer.MAX_VALUE;
        }

        return point.getEras().stream()
                .map(Era::getDisplayOrder)
                .filter(order -> order != null)
                .min(Integer::compareTo)
                .orElse(Integer.MAX_VALUE);
    }
}