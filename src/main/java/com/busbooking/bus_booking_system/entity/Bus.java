package com.busbooking.bus_booking_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "bus",
        indexes = {
                @Index(name = "idx_bus_from_location", columnList = "from_location"),
                @Index(name = "idx_bus_to_location", columnList = "to_location"),
                @Index(name = "idx_bus_route", columnList = "from_location,to_location"),
                @Index(name = "idx_bus_circuit", columnList = "circuit_id"),
                @Index(name = "idx_bus_active", columnList = "active")
        }
)
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // ROUTE INFORMATION
    // =========================================================

    @Column(name = "from_location", nullable = false, length = 150)
    private String fromLocation;

    @Column(name = "to_location", nullable = false, length = 150)
    private String toLocation;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    /**
     * Base seat price
     */
    @Column(nullable = false)
    private double price;

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    /**
     * Optional SEO slug
     * Example: delhi-nalanda-heritage-route
     */
    @Column(length = 200)
    private String slug;

    /**
     * Soft enable/disable bus
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Version field for future seat-layout changes
     */
    @Column(name = "seat_layout_version")
    private Integer seatLayoutVersion;

    /**
     * Creation timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // =========================================================
    // HERITAGE CIRCUIT
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "circuit_id")
    private Circuit circuit;

    // =========================================================
    // SCHEDULES
    // =========================================================

    @OneToMany(
            mappedBy = "bus",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<BusSchedule> schedules = new ArrayList<>();

    // =========================================================
    // ROUTE STOPS (Ordered for Map Rendering)
    // =========================================================

    @OneToMany(
            mappedBy = "bus",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("sequenceOrder ASC")
    @Builder.Default
    private List<RouteStop> routeStops = new ArrayList<>();

    // =========================================================
    // DERIVED FIELD
    // =========================================================

    /**
     * Calculate travel duration dynamically
     */
    @Transient
    public long getTravelDurationMinutes() {
        if (departureTime == null || arrivalTime == null) {
            return 0;
        }
        return Duration.between(departureTime, arrivalTime).toMinutes();
    }

    // =========================================================
    // AUTO TIMESTAMP
    // =========================================================

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.active == null) {
            this.active = true;
        }
    }
}
