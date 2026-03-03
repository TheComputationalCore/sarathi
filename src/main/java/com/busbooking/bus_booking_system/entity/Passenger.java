package com.busbooking.bus_booking_system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(
        name = "passenger",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_schedule_seat",
                        columnNames = {"bus_schedule_id", "seat_number"}
                )
        },
        indexes = {
                @Index(name = "idx_passenger_schedule", columnList = "bus_schedule_id")
        }
)
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int age;

    /**
     * Seat number within this schedule
     */
    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    /**
     * Passenger belongs to a specific booking
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    /**
     * Passenger belongs to a specific bus schedule (bus + date)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_schedule_id", nullable = false)
    private BusSchedule busSchedule;
}
