package com.busbooking.bus_booking_system.service.trail.strategy;

import com.busbooking.bus_booking_system.entity.YatraPoint;

import java.util.List;
import java.util.Set;

public interface TrailScoringStrategy {

    List<ScoredPoint> score(
            List<YatraPoint> points,
            Set<Long> selectedThemeIds
    );

    record ScoredPoint(YatraPoint point, double score) {}
}