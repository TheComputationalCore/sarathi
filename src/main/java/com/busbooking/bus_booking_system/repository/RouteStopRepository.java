package com.busbooking.bus_booking_system.repository;

import com.busbooking.bus_booking_system.entity.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {

    List<RouteStop> findByBusCircuitId(Long circuitId);

    List<RouteStop> findByYatraPointId(Long yatraPointId);
}
