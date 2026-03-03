package com.busbooking.bus_booking_system.service.trail.optimizer;

import com.busbooking.bus_booking_system.entity.YatraPoint;

import java.util.List;

public interface GeographicOptimizer {

    List<YatraPoint> optimize(
            List<YatraPoint> sortedPoints,
            int limit
    );
}