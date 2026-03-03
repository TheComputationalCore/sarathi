package com.busbooking.bus_booking_system.controller;

import com.busbooking.bus_booking_system.dto.response.AdminDashboardResponse;
import com.busbooking.bus_booking_system.dto.response.AdminUserResponseDTO;
import com.busbooking.bus_booking_system.dto.response.BookingResponseDTO;
import com.busbooking.bus_booking_system.entity.Bus;
import com.busbooking.bus_booking_system.service.AdminAnalyticsService;
import com.busbooking.bus_booking_system.service.AdminService;
import com.busbooking.bus_booking_system.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final BookingService bookingService;
    private final AdminAnalyticsService adminAnalyticsService;

    public AdminController(AdminService adminService,
                           BookingService bookingService,
                           AdminAnalyticsService adminAnalyticsService) {
        this.adminService = adminService;
        this.bookingService = bookingService;
        this.adminAnalyticsService = adminAnalyticsService;
    }

    // =====================================================
    // BUS MANAGEMENT
    // =====================================================

    @PostMapping("/buses")
    public ResponseEntity<Bus> addBus(@Valid @RequestBody Bus bus) {
        return ResponseEntity.ok(adminService.addBus(bus));
    }

    @PutMapping("/buses/{busId}")
    public ResponseEntity<Bus> updateBus(
            @PathVariable Long busId,
            @Valid @RequestBody Bus bus
    ) {
        return ResponseEntity.ok(adminService.updateBus(busId, bus));
    }

    @DeleteMapping("/buses/{busId}")
    public ResponseEntity<String> deleteBus(@PathVariable Long busId) {
        adminService.deleteBus(busId);
        return ResponseEntity.ok("Bus deleted successfully");
    }

    // =====================================================
    // BOOKING MANAGEMENT
    // =====================================================

    @GetMapping("/bookings")
    public ResponseEntity<Page<BookingResponseDTO>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "bookingTime")
        );

        Page<BookingResponseDTO> response = adminService
                .getAllBookings(pageable)
                .map(bookingService::mapToBookingResponse);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<String> cancelAnyBooking(@PathVariable Long bookingId) {
        adminService.cancelAnyBooking(bookingId);
        return ResponseEntity.ok("Booking cancelled by admin");
    }

    // =====================================================
    // ADMIN USERS MANAGEMENT (FINAL)
    // =====================================================

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserResponseDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        Page<AdminUserResponseDTO> users = adminService.getAllUsers(pageable);

        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<AdminUserResponseDTO> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUserById(userId));
    }

    // =====================================================
    // SYSTEM DASHBOARD (MAIN ANALYTICS)
    // =====================================================

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminAnalyticsService.getDashboardStats());
    }

    // =====================================================
    // LEGACY STATS (OPTIONAL)
    // =====================================================

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }
}
