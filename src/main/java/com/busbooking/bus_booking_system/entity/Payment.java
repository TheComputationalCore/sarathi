package com.busbooking.bus_booking_system.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_order_id", columnList = "razorpay_order_id"),
                @Index(name = "idx_payment_created_at", columnList = "created_at")
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One payment per booking
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "razorpay_order_id", nullable = false)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(nullable = false)
    private Integer amount; // in paise

    @Column(nullable = false)
    private String status; // CREATED, SUCCESS, FAILED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "CREATED";
        }
    }
}
