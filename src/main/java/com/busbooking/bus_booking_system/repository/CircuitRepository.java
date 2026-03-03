package com.busbooking.bus_booking_system.repository;

import com.busbooking.bus_booking_system.entity.Circuit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CircuitRepository extends JpaRepository<Circuit, Long> {

    // =========================================================
    // PUBLIC LOOKUPS
    // =========================================================

    // Fetch only active circuits (Homepage)
    List<Circuit> findByActiveTrueOrderByNameAsc();

    // Fetch active circuit by slug (SEO route)
    Optional<Circuit> findBySlugAndActiveTrue(String slug);

    // =========================================================
    // ADMIN LOOKUPS
    // =========================================================

    // Fetch by slug regardless of active status
    Optional<Circuit> findBySlug(String slug);

    // Fetch all active
    List<Circuit> findByActiveTrue();
}
