package com.busbooking.bus_booking_system.service.trail.optimizer;

import com.busbooking.bus_booking_system.entity.Era;
import com.busbooking.bus_booking_system.entity.YatraPoint;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class HaversineGeographicOptimizer implements GeographicOptimizer {

    private static final double DISTANCE_WEIGHT = 1.0;
    private static final double ERA_WEIGHT = 0.6;

    @Override
    public List<YatraPoint> optimize(
            List<YatraPoint> sortedPoints,
            int limit
    ) {

        if (sortedPoints == null || sortedPoints.isEmpty()) {
            return Collections.emptyList();
        }

        List<YatraPoint> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();

        YatraPoint current = sortedPoints.get(0);
        result.add(current);
        visited.add(current.getId());

        while (result.size() < limit) {

            YatraPoint next = findBestCandidate(
                    current,
                    sortedPoints,
                    visited
            );

            if (next == null) break;

            result.add(next);
            visited.add(next.getId());
            current = next;
        }

        return result;
    }

    private YatraPoint findBestCandidate(
            YatraPoint current,
            List<YatraPoint> candidates,
            Set<Long> visited
    ) {

        YatraPoint best = null;
        double bestScore = Double.MAX_VALUE;

        for (YatraPoint point : candidates) {

            if (visited.contains(point.getId())) continue;

            double distance = haversine(
                    current.getLatitude(),
                    current.getLongitude(),
                    point.getLatitude(),
                    point.getLongitude()
            );

            double chronologicalPenalty =
                    calculateChronologicalGap(current, point);

            double compositeScore =
                    (DISTANCE_WEIGHT * distance)
                            + (ERA_WEIGHT * chronologicalPenalty);

            if (compositeScore < bestScore) {
                bestScore = compositeScore;
                best = point;
            }
        }

        return best;
    }

    /**
     * Penalizes large historical jumps.
     * Uses midpoint year of each YatraPoint.
     */
    private double calculateChronologicalGap(
            YatraPoint a,
            YatraPoint b
    ) {

        Integer yearA = extractMidpointYear(a);
        Integer yearB = extractMidpointYear(b);

        if (yearA == null || yearB == null) {
            return 0; // no penalty if era data missing
        }

        return Math.abs(yearA - yearB) / 100.0; // scaled penalty
    }

    private Integer extractMidpointYear(YatraPoint point) {

        if (point.getEras() == null || point.getEras().isEmpty()) {
            return null;
        }

        int sum = 0;
        int count = 0;

        for (Era era : point.getEras()) {

            if (era.getStartYear() != null && era.getEndYear() != null) {
                int midpoint = (era.getStartYear() + era.getEndYear()) / 2;
                sum += midpoint;
                count++;
            }
        }

        if (count == 0) return null;

        return sum / count;
    }

    private double haversine(
            double lat1, double lon1,
            double lat2, double lon2
    ) {

        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}