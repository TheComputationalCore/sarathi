package com.busbooking.bus_booking_system.repository;

import com.busbooking.bus_booking_system.entity.Era;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EraRepository extends JpaRepository<Era, Long> {

    /**
     * Find era by exact name (case sensitive)
     */
    Optional<Era> findByName(String name);

    /**
     * Find eras by name (case-insensitive)
     */
    List<Era> findByNameIgnoreCaseIn(List<String> names);

    /**
     * Find all eras ordered chronologically
     */
    List<Era> findAllByOrderByDisplayOrderAsc();
}