package com.busbooking.bus_booking_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "eras",
        indexes = {
                @Index(name = "idx_era_name", columnList = "name"),
                @Index(name = "idx_era_period", columnList = "start_year, end_year"),
                @Index(name = "idx_era_display_order", columnList = "display_order")
        }
)
public class Era {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // BASIC INFORMATION
    // =========================================================

    @Column(nullable = false, length = 150, unique = true)
    private String name;

    @Column(name = "start_year")
    private Integer startYear;

    @Column(name = "end_year")
    private Integer endYear;

    @Column(length = 2000)
    private String summary;

    // =========================================================
    // INTELLIGENCE METADATA (NEW)
    // =========================================================

    /**
     * Chronological sequencing for timeline rendering
     */
    @Column(name = "display_order")
    private Integer displayOrder;

    /**
     * Civilizational weight (0.0 - 1.0)
     * Used in scoring algorithm
     */
    @Column(name = "civilizational_weight")
    private Double civilizationalWeight;

    @ManyToMany(mappedBy = "eras")
    @Builder.Default
    private Set<YatraPoint> yatraPoints = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (this.civilizationalWeight == null) {
            this.civilizationalWeight = 0.5;
        }

        if (this.displayOrder == null) {
            this.displayOrder = 0;
        }
    }
}
