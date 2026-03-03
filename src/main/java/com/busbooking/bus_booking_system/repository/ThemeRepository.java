package com.busbooking.bus_booking_system.repository;

import com.busbooking.bus_booking_system.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    List<Theme> findByNameIn(List<String> names);
}