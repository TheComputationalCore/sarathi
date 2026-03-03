package com.busbooking.bus_booking_system.service.trail.strategy;

import com.busbooking.bus_booking_system.entity.Era;
import com.busbooking.bus_booking_system.entity.YatraPoint;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DefaultTrailScoringStrategy implements TrailScoringStrategy {

    @Override
    public List<ScoredPoint> score(
            List<YatraPoint> points,
            Set<Long> selectedThemeIds
    ) {

        List<ScoredPoint> scored = new ArrayList<>();

        for (YatraPoint point : points) {

            double score = calculateScore(point, selectedThemeIds);

            if (score > 0) {
                scored.add(new ScoredPoint(point, score));
            }
        }

        // Sort by score DESC, then by name ASC (deterministic ordering)
        scored.sort(
                Comparator.comparingDouble(ScoredPoint::score)
                        .reversed()
                        .thenComparing(sp -> sp.point().getName())
        );

        return scored;
    }

    private double calculateScore(
            YatraPoint point,
            Set<Long> selectedThemeIds
    ) {

        if (point.getThemes() == null || point.getThemes().isEmpty()) {
            return 0;
        }

        // ==============================
        // 1️⃣ THEME MATCH SCORE (0–1)
        // ==============================

        long matchedThemes = point.getThemes().stream()
                .filter(t -> selectedThemeIds.contains(t.getId()))
                .count();

        if (matchedThemes == 0) {
            return 0;
        }

        double themeScore = (double) matchedThemes / selectedThemeIds.size();


        // ==============================
        // 2️⃣ CULTURAL SIGNIFICANCE (0–1)
        // ==============================

        Integer cultural = point.getCulturalSignificanceScore();
        double culturalScore = cultural != null
                ? Math.min(cultural / 100.0, 1.0)
                : 0.3; // default baseline


        // ==============================
        // 3️⃣ ERA CIVILIZATIONAL WEIGHT (0–1)
        // ==============================

        double eraScore = 0;

        if (point.getEras() != null && !point.getEras().isEmpty()) {
            eraScore = point.getEras().stream()
                    .map(Era::getCivilizationalWeight)
                    .filter(Objects::nonNull)
                    .max(Double::compareTo)
                    .orElse(0.5); // fallback if era exists but weight null

            eraScore = Math.min(eraScore, 1.0);
        }


        // ==============================
        // 4️⃣ POPULARITY NORMALIZED (0–1)
        // ==============================

        Integer popularity = point.getPopularityScore();
        double popularityScore = popularity != null
                ? Math.min(popularity / 100.0, 1.0)
                : 0.2;


        // ==============================
        // FINAL WEIGHTED SCORE
        // ==============================

        return
                (themeScore * 0.40) +
                (culturalScore * 0.25) +
                (eraScore * 0.20) +
                (popularityScore * 0.15);
    }
}