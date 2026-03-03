package com.busbooking.bus_booking_system.repository;

import com.busbooking.bus_booking_system.dto.response.AdminUserResponseDTO;
import com.busbooking.bus_booking_system.entity.Role;
import com.busbooking.bus_booking_system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // ================= AUTH =================

    Optional<User> findByEmail(String email);

    Optional<User> findByName(String name);

    // ================= ADMIN USERS (BASIC) =================

    Page<User> findAll(Pageable pageable);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // ================= ADMIN USERS (ADVANCED WITH BOOKING COUNT) =================

    @Query("""
        SELECT new com.busbooking.bus_booking_system.dto.response.AdminUserResponseDTO(
            u.id,
            u.name,
            u.email,
            u.role,
            COUNT(b)
        )
        FROM User u
        LEFT JOIN Booking b ON b.user = u
        GROUP BY u.id, u.name, u.email, u.role
        ORDER BY u.id DESC
    """)
    Page<AdminUserResponseDTO> findAllUsersWithBookingCount(Pageable pageable);
}
