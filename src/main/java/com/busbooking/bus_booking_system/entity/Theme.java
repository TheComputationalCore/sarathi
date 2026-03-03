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
        name = "themes",
        indexes = {
                @Index(name = "idx_theme_name", columnList = "name")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_theme_name", columnNames = {"name"})
        }
)
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String icon;

    @Column(name = "priority_weight")
    @Builder.Default
    private Integer priorityWeight = 0;

    @ManyToMany(mappedBy = "themes")
    @Builder.Default
    private Set<YatraPoint> yatraPoints = new HashSet<>();

    @ManyToMany(mappedBy = "themes")
    @Builder.Default
    private Set<Circuit> circuits = new HashSet<>();
}
