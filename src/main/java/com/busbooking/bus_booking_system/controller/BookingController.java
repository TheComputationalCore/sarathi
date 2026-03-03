package com.busbooking.bus_booking_system.controller;

import com.busbooking.bus_booking_system.dto.response.BookingResponseDTO;
import com.busbooking.bus_booking_system.entity.Booking;
import com.busbooking.bus_booking_system.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // ================= CREATE BOOKING =================

    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(
            @Valid @RequestBody BookingRequest bookingRequest,
            Authentication authentication
    ) {

        Booking booking = bookingService.createBooking(
                bookingRequest,
                authentication.getName()
        );

        BookingResponseDTO response =
                bookingService.mapToBookingResponse(booking);

        return ResponseEntity.ok(response);
    }

    // ================= BOOKING HISTORY =================

    @GetMapping("/history")
    public ResponseEntity<List<BookingResponseDTO>> getBookingHistory(
            Authentication authentication
    ) {

        List<BookingResponseDTO> response =
                bookingService.getBookingHistory(authentication.getName());

        return ResponseEntity.ok(response);
    }

    // ================= CANCEL BOOKING =================

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<String> cancelBooking(
            @PathVariable Long bookingId,
            Authentication authentication
    ) {

        bookingService.cancelBooking(bookingId, authentication.getName());

        return ResponseEntity.ok("Booking cancelled successfully");
    }
}
