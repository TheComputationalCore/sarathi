package com.busbooking.bus_booking_system.service.trail.narrative;

import com.busbooking.bus_booking_system.entity.Theme;
import com.busbooking.bus_booking_system.entity.YatraPoint;

import java.util.List;

public interface NarrativeComposer {

    String buildTrailName(
            List<Theme> themes,
            List<YatraPoint> stops
    );

    String buildNarrativeSummary(
            List<Theme> themes,
            List<YatraPoint> stops
    );
}