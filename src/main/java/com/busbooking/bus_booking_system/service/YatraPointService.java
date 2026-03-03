package com.busbooking.bus_booking_system.service;

import com.busbooking.bus_booking_system.dto.response.BusResponseDTO;
import com.busbooking.bus_booking_system.dto.response.YatraPointWithBusesDTO;
import com.busbooking.bus_booking_system.entity.RouteStop;
import com.busbooking.bus_booking_system.entity.YatraPoint;
import com.busbooking.bus_booking_system.repository.RouteStopRepository;
import com.busbooking.bus_booking_system.repository.YatraPointRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class YatraPointService {

    private final YatraPointRepository yatraPointRepository;
    private final RouteStopRepository routeStopRepository;

    public YatraPointService(YatraPointRepository yatraPointRepository,
                             RouteStopRepository routeStopRepository) {
        this.yatraPointRepository = yatraPointRepository;
        this.routeStopRepository = routeStopRepository;
    }

    /**
     * Fetch all active YatraPoints
     * Optional circuit filtering
     */
    public List<YatraPointWithBusesDTO> getYatraPoints(Long circuitId) {

        List<YatraPoint> points = yatraPointRepository.findByActiveTrue();

        return points.stream()
                .map(point -> mapToDTO(point, circuitId))
                .sorted(Comparator.comparing(YatraPointWithBusesDTO::name))
                .toList();
    }

    private YatraPointWithBusesDTO mapToDTO(YatraPoint point, Long circuitId) {

        List<RouteStop> stops;

        if (circuitId != null) {
            stops = routeStopRepository.findByBusCircuitId(circuitId)
                    .stream()
                    .filter(stop -> stop.getYatraPoint().getId().equals(point.getId()))
                    .toList();
        } else {
            stops = routeStopRepository.findByYatraPointId(point.getId());
        }

        List<BusResponseDTO> buses = stops.stream()
                .map(stop -> stop.getBus())
                .distinct()
                .map(bus -> new BusResponseDTO(
                        bus.getId(),
                        bus.getFromLocation(),
                        bus.getToLocation(),
                        bus.getDepartureTime(),
                        bus.getArrivalTime(),
                        bus.getPrice(),
                        bus.getTotalSeats()
                ))
                .collect(Collectors.toList());

        return new YatraPointWithBusesDTO(
                point.getId(),
                point.getName(),
                point.getSlug(),
                point.getShortHistory(),
                point.getLatitude(),
                point.getLongitude(),
                point.getImageUrl(),
                buses
        );
    }
}
