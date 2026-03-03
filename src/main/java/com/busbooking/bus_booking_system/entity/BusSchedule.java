package com.busbooking.bus_booking_system.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(
        name = "bus_schedule",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bus_travel_date",
                        columnNames = {"bus_id", "travel_date"}
                )
        },
        indexes = {
                @Index(name = "idx_schedule_date", columnList = "travel_date")
        }
)
public class BusSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Which bus this schedule belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    /**
     * Travel date for this bus run
     */
    @Column(name = "travel_date", nullable = false)
    private LocalDate travelDate;

    /**
     * Live seat inventory for this specific date.
     * This value is decremented/incremented during booking & cancellation.
     */
    @Column(nullable = false)
    private int availableSeats;

    /**
     * One schedule has many passengers.
     * Passenger uniqueness will be per schedule + seatNumber.
     */
    @OneToMany(
            mappedBy = "busSchedule",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Passenger> passengers = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (availableSeats < 0) {
            availableSeats = 0;
        }
    }
}
