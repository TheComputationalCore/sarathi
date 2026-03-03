package com.busbooking.bus_booking_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "circuits",
        indexes = {
                @Index(name = "idx_circuit_slug", columnList = "slug"),
                @Index(name = "idx_circuit_active", columnList = "active"),
                @Index(name = "idx_circuit_display_order", columnList = "display_order")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_circuit_slug",
                        columnNames = {"slug"}
                )
        }
)
public class Circuit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 150)
    private String slug;

    @Column(length = 2000)
    private String description;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "banner_image_url", length = 500)
    private String bannerImageUrl;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "meta_title", length = 255)
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "circuit", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Bus> buses = new ArrayList<>();

    // ================= NEW RELATION =================

    @ManyToMany
    @JoinTable(
            name = "circuit_themes",
            joinColumns = @JoinColumn(name = "circuit_id"),
            inverseJoinColumns = @JoinColumn(name = "theme_id")
    )
    @Builder.Default
    private Set<Theme> themes = new HashSet<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.active == null) this.active = true;
        if (this.displayOrder == null) this.displayOrder = 0;
    }
}
