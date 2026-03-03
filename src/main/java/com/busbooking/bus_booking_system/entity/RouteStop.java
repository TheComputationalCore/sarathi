package com.busbooking.bus_booking_system.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "route_stops",
        indexes = {
                @Index(name = "idx_route_stop_bus", columnList = "bus_id"),
                @Index(name = "idx_route_stop_sequence", columnList = "sequence_order"),
                @Index(name = "idx_route_stop_yatra_point", columnList = "yatra_point_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_route_stop_order",
                        columnNames = {"bus_id", "sequence_order"}
                )
        }
)
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Bus this stop belongs to.
     * LAZY to avoid circular JSON explosion.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    /**
     * Heritage / GIS Node.
     * This links to lat/lng via YatraPoint.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "yatra_point_id", nullable = false)
    private YatraPoint yatraPoint;

    /**
     * Order of stop within route.
     * Starts from 0 or 1 depending on convention.
     */
    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    /**
     * Optional: travel time from previous stop (in minutes).
     * Enables ETA calculation in future.
     */
    @Column(name = "travel_minutes_from_previous")
    private Integer travelMinutesFromPrevious;

    /**
     * Optional: distance from previous stop (in KM).
     * Enables map metrics and analytics.
     */
    @Column(name = "distance_km_from_previous")
    private Double distanceKmFromPrevious;
}