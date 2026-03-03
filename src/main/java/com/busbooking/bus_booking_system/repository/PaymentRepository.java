package com.busbooking.bus_booking_system.repository;

import com.busbooking.bus_booking_system.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // =========================================================
    // BASIC LOOKUPS
    // =========================================================

    Optional<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    // Lock payment row for correction
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p FROM Payment p
        WHERE p.id = :paymentId
    """)
    Optional<Payment> findByIdForUpdate(@Param("paymentId") Long paymentId);


    // =========================================================
    // RECONCILIATION SUPPORT (SELF-HEALING)
    // =========================================================

    /**
     * Payments stuck in CREATED or FAILED
     * older than cutoff time.
     */
    @Query("""
        SELECT p FROM Payment p
        WHERE p.status <> 'SUCCESS'
        AND p.createdAt < :cutoff
    """)
    List<Payment> findPendingPaymentsForReconciliation(
            @Param("cutoff") LocalDateTime cutoff
    );


    // =========================================================
    // REVENUE ANALYTICS (ADMIN DASHBOARD)
    // =========================================================

    /**
     * Total successful revenue (in paise)
     */
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.status = 'SUCCESS'
    """)
    Long getTotalRevenue();


    /**
     * Today's revenue (in paise)
     */
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.status = 'SUCCESS'
        AND p.paymentTime >= :startOfDay
    """)
    Long getTodayRevenue(@Param("startOfDay") LocalDateTime startOfDay);


    // =========================================================
    // COUNTING (USEFUL FOR ADMIN METRICS)
    // =========================================================

    long countByStatus(String status);

    long countByStatusAndCreatedAtAfter(String status, LocalDateTime time);
}