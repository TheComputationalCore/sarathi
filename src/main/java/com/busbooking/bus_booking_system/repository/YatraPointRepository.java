package com.busbooking.bus_booking_system.repository;

import com.busbooking.bus_booking_system.entity.YatraPoint;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface YatraPointRepository extends JpaRepository<YatraPoint, Long> {

    // =========================================================
    // BASIC LOOKUPS
    // =========================================================

    List<YatraPoint> findByActiveTrue();

    Optional<YatraPoint> findBySlug(String slug);

    // =========================================================
    // ELITE FETCH METHODS (NO N+1)
    // =========================================================

    /**
     * Fetch active YatraPoints with themes eagerly loaded.
     * Used for trail generation.
     */
    @Query("""
        SELECT DISTINCT yp
        FROM YatraPoint yp
        LEFT JOIN FETCH yp.themes
        WHERE yp.active = true
    """)
    List<YatraPoint> findActiveWithThemes();

    /**
     * Fetch active YatraPoints with themes and eras.
     * Future-proof for era-based filtering.
     */
    @Query("""
        SELECT DISTINCT yp
        FROM YatraPoint yp
        LEFT JOIN FETCH yp.themes
        LEFT JOIN FETCH yp.eras
        WHERE yp.active = true
    """)
    List<YatraPoint> findActiveWithThemesAndEras();
}