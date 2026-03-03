package com.busbooking.bus_booking_system.service;

import com.busbooking.bus_booking_system.dto.response.CircuitResponseDTO;
import com.busbooking.bus_booking_system.entity.Circuit;
import com.busbooking.bus_booking_system.exception.ResourceNotFoundException;
import com.busbooking.bus_booking_system.repository.CircuitRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class CircuitService {

    private final CircuitRepository circuitRepository;

    public CircuitService(CircuitRepository circuitRepository) {
        this.circuitRepository = circuitRepository;
    }

    /**
     * Public endpoint
     * Returns only ACTIVE circuits
     */
    @Cacheable(value = "circuits")
    public List<CircuitResponseDTO> getActiveCircuits() {

        return circuitRepository.findByActiveTrue()
                .stream()
                .sorted(Comparator.comparing(Circuit::getName))
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Fetch circuit by slug
     */
    @Cacheable(value = "circuit", key = "#slug")
    public CircuitResponseDTO getBySlug(String slug) {

        Circuit circuit = circuitRepository.findBySlug(slug)
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .orElseThrow(() ->
                        new ResourceNotFoundException("Circuit not found or inactive"));

        return mapToDTO(circuit);
    }

    /**
     * Admin save
     */
    @CacheEvict(value = {"circuits", "circuit"}, allEntries = true)
    public CircuitResponseDTO save(Circuit circuit) {

        Circuit saved = circuitRepository.save(circuit);
        return mapToDTO(saved);
    }

    /**
     * Admin delete
     */
    @CacheEvict(value = {"circuits", "circuit"}, allEntries = true)
    public void delete(Long id) {

        circuitRepository.deleteById(id);
    }

    private CircuitResponseDTO mapToDTO(Circuit circuit) {

        return new CircuitResponseDTO(
                circuit.getId(),
                circuit.getName(),
                circuit.getSlug(),
                circuit.getDescription(),
                circuit.getIconUrl(),
                circuit.getBannerImageUrl()
        );
    }
}
