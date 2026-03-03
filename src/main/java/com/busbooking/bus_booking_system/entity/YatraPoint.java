package com.busbooking.bus_booking_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "yatra_points",
        indexes = {
                @Index(name = "idx_yatra_slug", columnList = "slug"),
                @Index(name = "idx_yatra_active", columnList = "active"),
                @Index(name = "idx_yatra_latitude", columnList = "latitude"),
                @Index(name = "idx_yatra_longitude", columnList = "longitude")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_yatra_slug",
                        columnNames = {"slug"}
                )
        }
)
public class YatraPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // BASIC INFORMATION
    // =========================================================

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 150)
    private String slug;

    @Column(length = 4000)
    private String shortHistory;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "recommended_zoom_level")
    private Integer recommendedZoomLevel;

    @Column(length = 500)
    private String imageUrl;

    // =========================================================
    // INTELLIGENCE SCORES (NEW)
    // =========================================================

    @Column(name = "popularity_score")
    private Integer popularityScore;

    @Column(name = "cultural_significance_score")
    private Integer culturalSignificanceScore;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(length = 300)
    private String metaDescription;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // =========================================================
    // RELATIONSHIPS
    // =========================================================

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "yatra_point_themes",
            joinColumns = @JoinColumn(name = "yatra_point_id"),
            inverseJoinColumns = @JoinColumn(name = "theme_id")
    )
    @Builder.Default
    private Set<Theme> themes = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "yatra_point_eras",
            joinColumns = @JoinColumn(name = "yatra_point_id"),
            inverseJoinColumns = @JoinColumn(name = "era_id")
    )
    @Builder.Default
    private Set<Era> eras = new HashSet<>();

    // =========================================================
    // AUTO TIMESTAMP + DEFAULTS
    // =========================================================

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.active == null) {
            this.active = true;
        }

        if (this.popularityScore == null) {
            this.popularityScore = 0;
        }

        if (this.culturalSignificanceScore == null) {
            this.culturalSignificanceScore = 50; // balanced baseline
        }
    }
}
