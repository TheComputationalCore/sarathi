package com.busbooking.bus_booking_system.repository;

import com.busbooking.bus_booking_system.entity.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // =========================================================
    // IDEMPOTENCY LOOKUP
    // =========================================================

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);

    // =========================================================
    // 🔒 PESSIMISTIC LOCK - BASIC
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b FROM Booking b
        WHERE b.id = :bookingId
    """)
    Optional<Booking> findByIdForUpdate(
            @Param("bookingId") Long bookingId
    );

    // =========================================================
    // 🔒 PESSIMISTIC LOCK - FULL GRAPH
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "passengers",
            "passengers.busSchedule",
            "bus",
            "payment",
            "user"
    })
    @Query("""
        SELECT b FROM Booking b
        WHERE b.id = :bookingId
    """)
    Optional<Booking> findByIdWithDetailsForUpdate(
            @Param("bookingId") Long bookingId
    );

    // =========================================================
    // USER BOOKING HISTORY
    // =========================================================

    @EntityGraph(attributePaths = {
            "passengers",
            "passengers.busSchedule",
            "bus"
    })
    @Query("""
        SELECT DISTINCT b FROM Booking b
        WHERE b.user.id = :userId
        ORDER BY b.bookingTime DESC
    """)
    List<Booking> findByUserId(
            @Param("userId") Long userId
    );

    // =========================================================
    // ADMIN PAGINATED VIEW
    // =========================================================

    @EntityGraph(attributePaths = {
            "user",
            "bus",
            "passengers",
            "passengers.busSchedule"
    })
    @Query(
        value = """
            SELECT b FROM Booking b
            ORDER BY b.bookingTime DESC
        """,
        countQuery = """
            SELECT COUNT(b) FROM Booking b
        """
    )
    Page<Booking> findAllBookings(Pageable pageable);

    // =========================================================
    // SECURE OWNERSHIP FETCH
    // =========================================================

    @EntityGraph(attributePaths = {
            "passengers",
            "passengers.busSchedule",
            "bus"
    })
    @Query("""
        SELECT DISTINCT b FROM Booking b
        WHERE b.id = :bookingId
        AND b.user.id = :userId
    """)
    Optional<Booking> findByIdAndUserId(
            @Param("bookingId") Long bookingId,
            @Param("userId") Long userId
    );

    // =========================================================
    // AUTO EXPIRE UNPAID BOOKINGS (ID-FIRST STRATEGY)
    // =========================================================

    @Query("""
        SELECT b.id FROM Booking b
        WHERE b.status = 'PAYMENT_PENDING'
        AND b.bookingTime < :cutoff
    """)
    List<Long> findExpiredPendingBookingIds(
            @Param("cutoff") LocalDateTime cutoff
    );

    // =========================================================
    // OPTIONAL: FULL FETCH FOR SCHEDULER (NON-LOCKING)
    // =========================================================

    @EntityGraph(attributePaths = {
            "passengers",
            "passengers.busSchedule",
            "bus"
    })
    @Query("""
        SELECT b FROM Booking b
        WHERE b.status = 'PAYMENT_PENDING'
        AND b.bookingTime < :cutoff
    """)
    List<Booking> findExpiredPendingBookings(
            @Param("cutoff") LocalDateTime cutoff
    );

    // =========================================================
    // READ-ONLY FULL DETAILS
    // =========================================================

    @EntityGraph(attributePaths = {
            "passengers",
            "passengers.busSchedule",
            "bus",
            "payment",
            "user"
    })
    @Query("""
        SELECT b FROM Booking b
        WHERE b.id = :bookingId
    """)
    Optional<Booking> findByIdWithDetails(
            @Param("bookingId") Long bookingId
    );

    // =========================================================
    // ANALYTICS
    // =========================================================

    long countByStatus(String status);
    long countByUser_Id(Long userId);

    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.bookingTime >= :startOfDay
    """)
    long countTodayBookings(
            @Param("startOfDay") LocalDateTime startOfDay
    );

    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.status = 'CONFIRMED'
    """)
    long countConfirmedBookings();

    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.status = 'CANCELLED'
    """)
    long countCancelledBookings();
}
