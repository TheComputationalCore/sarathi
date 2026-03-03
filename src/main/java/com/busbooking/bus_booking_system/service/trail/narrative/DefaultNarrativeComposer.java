package com.busbooking.bus_booking_system.service.trail.narrative;

import com.busbooking.bus_booking_system.entity.Era;
import com.busbooking.bus_booking_system.entity.Theme;
import com.busbooking.bus_booking_system.entity.YatraPoint;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DefaultNarrativeComposer implements NarrativeComposer {

    @Override
    public String buildTrailName(
            List<Theme> themes,
            List<YatraPoint> stops
    ) {

        String dominantTheme = themes.stream()
                .map(Theme::getName)
                .findFirst()
                .orElse("Civilizational");

        String eraSpan = extractEraSpan(stops);

        if (eraSpan != null) {
            return dominantTheme + " Journey (" + eraSpan + ")";
        }

        return dominantTheme + " Civilizational Path";
    }

    @Override
    public String buildNarrativeSummary(
            List<Theme> themes,
            List<YatraPoint> stops
    ) {

        String themeLine = themes.stream()
                .map(Theme::getName)
                .collect(Collectors.joining(", "));

        int stopCount = stops.size();

        String eraSpan = extractEraSpan(stops);

        StringBuilder narrative = new StringBuilder();

        narrative.append("This curated journey explores the civilizational dimensions of ")
                .append(themeLine)
                .append(", unfolding across ")
                .append(stopCount)
                .append(" significant heritage nodes.");

        if (eraSpan != null) {
            narrative.append(" The path traces a historical arc spanning ")
                    .append(eraSpan)
                    .append(".");
        }

        narrative.append(" As you move geographically through these locations, ")
                .append("you also move through time — witnessing the evolution of ideas, ")
                .append("power structures, sacred geographies, and cultural memory across Bharat.");

        narrative.append(" This is not a sequence of destinations, but a narrative progression.");

        return narrative.toString();
    }

    private String extractEraSpan(List<YatraPoint> stops) {

        List<Integer> years = new ArrayList<>();

        for (YatraPoint point : stops) {
            if (point.getEras() == null) continue;

            for (Era era : point.getEras()) {

                if (era.getStartYear() != null) {
                    years.add(era.getStartYear());
                }

                if (era.getEndYear() != null) {
                    years.add(era.getEndYear());
                }
            }
        }

        if (years.isEmpty()) return null;

        int min = Collections.min(years);
        int max = Collections.max(years);

        return formatYear(min) + " to " + formatYear(max);
    }

    private String formatYear(int year) {

        if (year < 0) {
            return Math.abs(year) + " BCE";
        }

        return year + " CE";
    }
}