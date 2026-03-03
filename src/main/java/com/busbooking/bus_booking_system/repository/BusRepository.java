package com.busbooking.bus_booking_system.repository;

import com.busbooking.bus_booking_system.entity.Bus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BusRepository extends JpaRepository<Bus, Long> {

    // =========================================================
    // ACTIVE BUS LOOKUPS (CORE SEARCH LAYER)
    // =========================================================

    @Query("""
        SELECT b FROM Bus b
        WHERE b.active = true
    """)
    List<Bus> findAllActive();


    @Query("""
        SELECT b FROM Bus b
        WHERE b.id = :id
        AND b.active = true
    """)
    Optional<Bus> findActiveById(@Param("id") Long id);


    // =========================================================
    // ROUTE SEARCH (CASE NORMALIZED)
    // =========================================================

    @Query("""
        SELECT b FROM Bus b
        WHERE b.active = true
        AND LOWER(b.fromLocation) = :from
        AND LOWER(b.toLocation) = :to
    """)
    List<Bus> findActiveByRoute(
            @Param("from") String from,
            @Param("to") String to
    );


    // =========================================================
    // CIRCUIT FILTER
    // =========================================================

    @Query("""
        SELECT b FROM Bus b
        WHERE b.active = true
        AND b.circuit.id = :circuitId
    """)
    List<Bus> findActiveByCircuit(
            @Param("circuitId") Long circuitId
    );


    // =========================================================
    // CIRCUIT + ROUTE FILTER
    // =========================================================

    @Query("""
        SELECT b FROM Bus b
        WHERE b.active = true
        AND b.circuit.id = :circuitId
        AND LOWER(b.fromLocation) = :from
        AND LOWER(b.toLocation) = :to
    """)
    List<Bus> findActiveByCircuitAndRoute(
            @Param("circuitId") Long circuitId,
            @Param("from") String from,
            @Param("to") String to
    );

    // =========================================================
    // THEME FILTERS (via Circuit -> Themes)
    // =========================================================

    @Query("""
        SELECT DISTINCT b
        FROM Bus b
        JOIN b.circuit c
        JOIN c.themes t
        WHERE b.active = true
          AND LOWER(t.name) IN :themeNames
    """)
    List<Bus> findActiveByThemes(
            @Param("themeNames") Set<String> themeNames
    );

    @Query("""
        SELECT DISTINCT b
        FROM Bus b
        JOIN b.circuit c
        JOIN c.themes t
        WHERE b.active = true
          AND c.id = :circuitId
          AND LOWER(t.name) IN :themeNames
    """)
    List<Bus> findActiveByCircuitAndThemes(
            @Param("circuitId") Long circuitId,
            @Param("themeNames") Set<String> themeNames
    );

    @Query("""
        SELECT DISTINCT b
        FROM Bus b
        JOIN b.circuit c
        JOIN c.themes t
        WHERE b.active = true
          AND LOWER(b.fromLocation) = :from
          AND LOWER(b.toLocation) = :to
          AND LOWER(t.name) IN :themeNames
    """)
    List<Bus> findActiveByRouteAndThemes(
            @Param("from") String from,
            @Param("to") String to,
            @Param("themeNames") Set<String> themeNames
    );

    @Query("""
        SELECT DISTINCT b
        FROM Bus b
        JOIN b.circuit c
        JOIN c.themes t
        WHERE b.active = true
          AND c.id = :circuitId
          AND LOWER(b.fromLocation) = :from
          AND LOWER(b.toLocation) = :to
          AND LOWER(t.name) IN :themeNames
    """)
    List<Bus> findActiveByCircuitRouteAndThemes(
            @Param("circuitId") Long circuitId,
            @Param("from") String from,
            @Param("to") String to,
            @Param("themeNames") Set<String> themeNames
    );


    // =========================================================
    // MAP LAYER FETCH (NO N+1)
    // Fetch routeStops + yatraPoints eagerly
    // =========================================================

    @EntityGraph(attributePaths = {
            "routeStops",
            "routeStops.yatraPoint"
    })
    @Query("""
        SELECT b FROM Bus b
        WHERE b.id = :busId
    """)
    Optional<Bus> findByIdWithRouteStops(
            @Param("busId") Long busId
    );


    // =========================================================
    // BOOKING FLOW LOCK (CRITICAL SECTION)
    // Used during seat validation
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b FROM Bus b
        WHERE b.id = :busId
    """)
    Optional<Bus> findByIdForUpdate(
            @Param("busId") Long busId
    );
}
