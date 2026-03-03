package com.busbooking.bus_booking_system.service;

import com.busbooking.bus_booking_system.dto.RouteMapDTO;
import com.busbooking.bus_booking_system.entity.Bus;
import com.busbooking.bus_booking_system.entity.RouteStop;
import com.busbooking.bus_booking_system.exception.ResourceNotFoundException;
import com.busbooking.bus_booking_system.repository.BusRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class RouteMapService {

    private final BusRepository busRepository;

    public RouteMapService(BusRepository busRepository) {
        this.busRepository = busRepository;
    }

    public RouteMapDTO getRouteMap(Long busId) {

        Bus bus = busRepository.findByIdWithRouteStops(busId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Bus not found"));

        if (bus.getRouteStops() == null || bus.getRouteStops().isEmpty()) {
            throw new ResourceNotFoundException("No route stops defined for this bus");
        }

        List<RouteMapDTO.Point> points = bus.getRouteStops()
                .stream()
                .sorted(Comparator.comparing(RouteStop::getSequenceOrder))
                .map(stop -> {
                    var yp = stop.getYatraPoint();
                    return new RouteMapDTO.Point(
                            yp.getId(),
                            yp.getName(),
                            yp.getSlug(),
                            yp.getLatitude(),
                            yp.getLongitude(),
                            yp.getImageUrl(),
                            yp.getShortHistory(),
                            yp.getRecommendedZoomLevel()
                    );
                })
                .toList();

        return new RouteMapDTO(
                bus.getId(),
                bus.getFromLocation(),
                bus.getToLocation(),
                bus.getCircuit() != null ? bus.getCircuit().getName() : null,
                points
        );
    }
}
