package com.busbooking.bus_booking_system.repository;

import com.busbooking.bus_booking_system.entity.BusSchedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface BusScheduleRepository extends JpaRepository<BusSchedule, Long> {

    /**
     * Fetch schedule normally
     */
    Optional<BusSchedule> findByBusIdAndTravelDate(Long busId, LocalDate travelDate);

    /**
     * CRITICAL:
     * Fetch schedule with PESSIMISTIC_WRITE lock
     * Used during booking to prevent race conditions
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s FROM BusSchedule s
        WHERE s.bus.id = :busId
        AND s.travelDate = :travelDate
    """)
    Optional<BusSchedule> findByBusIdAndTravelDateForUpdate(
            @Param("busId") Long busId,
            @Param("travelDate") LocalDate travelDate
    );
}
