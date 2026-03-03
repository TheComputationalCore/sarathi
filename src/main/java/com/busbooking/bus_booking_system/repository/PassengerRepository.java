package com.busbooking.bus_booking_system.repository;

import com.busbooking.bus_booking_system.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    /**
     * Fetch passengers by booking
     */
    List<Passenger> findByBooking_Id(Long bookingId);

    /**
     * Fetch passengers by BusSchedule
     */
    List<Passenger> findByBusSchedule_Id(Long busScheduleId);
}
