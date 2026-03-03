package com.busbooking.bus_booking_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "booking",
        indexes = {
                @Index(name = "idx_booking_user_id", columnList = "user_id"),
                @Index(name = "idx_booking_bus_id", columnList = "bus_id"),
                @Index(name = "idx_booking_status", columnList = "status"),
                @Index(name = "idx_booking_time", columnList = "booking_time"),
                @Index(name = "idx_booking_idempotency_key", columnList = "idempotency_key")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_booking_idempotency",
                        columnNames = {"idempotency_key"}
                )
        }
)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= IDEMPOTENCY =================
    // Prevents duplicate booking creation
    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    // ================= BOOKING STATUS =================
    // PAYMENT_PENDING, CONFIRMED, CANCELLED, PAYMENT_FAILED
    @Column(nullable = false)
    private String status;

    @Column(name = "booking_time", nullable = false)
    private LocalDateTime bookingTime;

    // ================= PAYMENT INFO =================

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    // ================= RELATIONS =================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @OneToMany(
            mappedBy = "booking",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Passenger> passengers = new ArrayList<>();

    @OneToOne(
            mappedBy = "booking",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    private Payment payment;

    // ================= AUTO TIMESTAMP =================

    @PrePersist
    public void prePersist() {
        this.bookingTime = LocalDateTime.now();

        if (this.status == null) {
            this.status = "PAYMENT_PENDING";
        }
    }
}
