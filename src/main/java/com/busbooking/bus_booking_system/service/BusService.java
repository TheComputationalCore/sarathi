package com.busbooking.bus_booking_system.service;

import com.busbooking.bus_booking_system.dto.response.BusResponseDTO;
import com.busbooking.bus_booking_system.dto.RouteMapDTO;
import com.busbooking.bus_booking_system.entity.Bus;
import com.busbooking.bus_booking_system.entity.Circuit;
import com.busbooking.bus_booking_system.entity.RouteStop;
import com.busbooking.bus_booking_system.entity.YatraPoint;
import com.busbooking.bus_booking_system.exception.ResourceNotFoundException;
import com.busbooking.bus_booking_system.repository.BusRepository;
import com.busbooking.bus_booking_system.repository.CircuitRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BusService {

    private final BusRepository busRepository;
    private final CircuitRepository circuitRepository;

    public BusService(BusRepository busRepository,
                      CircuitRepository circuitRepository) {
        this.busRepository = busRepository;
        this.circuitRepository = circuitRepository;
    }

    // =========================================================
    // SEARCH ENGINE (ACTIVE + NORMALIZED)
    // =========================================================

    public List<BusResponseDTO> findBuses(
            String from,
            String to,
            Long circuitId,
            String circuitSlug,
            String themesCsv
    ) {

        String normalizedFrom = normalize(from);
        String normalizedTo = normalize(to);
        Set<String> normalizedThemes = normalizeThemes(themesCsv);

        Long resolvedCircuitId = resolveCircuit(circuitId, circuitSlug);

        List<Bus> buses;

        if (!normalizedThemes.isEmpty()
                && resolvedCircuitId != null
                && normalizedFrom != null
                && normalizedTo != null) {
            buses = busRepository.findActiveByCircuitRouteAndThemes(
                    resolvedCircuitId,
                    normalizedFrom,
                    normalizedTo,
                    normalizedThemes
            );

        } else if (!normalizedThemes.isEmpty() && resolvedCircuitId != null) {
            buses = busRepository.findActiveByCircuitAndThemes(
                    resolvedCircuitId,
                    normalizedThemes
            );

        } else if (!normalizedThemes.isEmpty()
                && normalizedFrom != null
                && normalizedTo != null) {
            buses = busRepository.findActiveByRouteAndThemes(
                    normalizedFrom,
                    normalizedTo,
                    normalizedThemes
            );

        } else if (!normalizedThemes.isEmpty()) {
            buses = busRepository.findActiveByThemes(normalizedThemes);

        } else if (resolvedCircuitId != null && normalizedFrom != null && normalizedTo != null) {
            buses = busRepository
                    .findActiveByCircuitAndRoute(
                            resolvedCircuitId,
                            normalizedFrom,
                            normalizedTo
                    );

        } else if (resolvedCircuitId != null) {
            buses = busRepository.findActiveByCircuit(resolvedCircuitId);

        } else if (normalizedFrom != null && normalizedTo != null) {
            buses = busRepository
                    .findActiveByRoute(normalizedFrom, normalizedTo);

        } else {
            buses = busRepository.findAllActive();
        }

        return buses.stream()
                .sorted(Comparator.comparing(Bus::getDepartureTime))
                .map(this::mapToDTO)
                .toList();
    }

    // =========================================================
    // FETCH SINGLE BUS (ACTIVE ONLY)
    // =========================================================

    public BusResponseDTO findById(Long id) {

        Bus bus = busRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));

        return mapToDTO(bus);
    }

    // =========================================================
    // MAP ROUTE LAYER (ELITE VERSION)
    // =========================================================

    @Cacheable(value = "route-map", key = "#busId")
    public RouteMapDTO getRouteMap(Long busId) {

        Bus bus = busRepository.findByIdWithRouteStops(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));

        List<RouteMapDTO.Point> points = bus.getRouteStops()
                .stream()
                .sorted(Comparator.comparing(RouteStop::getSequenceOrder))
                .map(stop -> {

                    YatraPoint yp = stop.getYatraPoint();

                    return new RouteMapDTO.Point(
                            yp.getId(),
                            yp.getName(),
                            yp.getSlug(),
                            yp.getLatitude(),
                            yp.getLongitude(),
                            yp.getImageUrl(),
                            yp.getShortHistory(),
                            13 // recommended default zoom (can be dynamic later)
                    );
                })
                .toList();

        String circuitName = bus.getCircuit() != null
                ? bus.getCircuit().getName()
                : null;

        return new RouteMapDTO(
                bus.getId(),
                bus.getFromLocation(),
                bus.getToLocation(),
                circuitName,
                points
        );
    }

    // =========================================================
    // ADMIN OPERATIONS
    // =========================================================

    @CacheEvict(value = {"buses", "bus", "route-map"}, allEntries = true)
    public BusResponseDTO saveBus(Bus bus) {

        Bus saved = busRepository.save(bus);
        return mapToDTO(saved);
    }

    @CacheEvict(value = {"buses", "bus", "route-map"}, allEntries = true)
    public void deleteBus(Long id) {
        busRepository.deleteById(id);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private Long resolveCircuit(Long circuitId, String slug) {

        if (circuitId != null && slug != null) {
            throw new IllegalArgumentException("Use either circuitId or circuitSlug");
        }

        if (circuitId != null) {
            Circuit circuit = circuitRepository.findById(circuitId)
                    .filter(c -> Boolean.TRUE.equals(c.getActive()))
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Circuit not found"));
            return circuit.getId();
        }

        if (slug != null && !slug.isBlank()) {
            Circuit circuit = circuitRepository.findBySlug(slug)
                    .filter(c -> Boolean.TRUE.equals(c.getActive()))
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Circuit not found"));
            return circuit.getId();
        }

        return null;
    }

    private String normalize(String input) {
        if (input == null || input.isBlank()) return null;
        return input.trim().toLowerCase(Locale.ROOT);
    }

    private Set<String> normalizeThemes(String themesCsv) {
        if (themesCsv == null || themesCsv.isBlank()) {
            return Set.of();
        }

        return List.of(themesCsv.split(","))
                .stream()
                .map(this::normalize)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.toSet());
    }

    private BusResponseDTO mapToDTO(Bus bus) {

        return new BusResponseDTO(
                bus.getId(),
                bus.getFromLocation(),
                bus.getToLocation(),
                bus.getDepartureTime(),
                bus.getArrivalTime(),
                bus.getPrice(),
                bus.getTotalSeats()
        );
    }
}
