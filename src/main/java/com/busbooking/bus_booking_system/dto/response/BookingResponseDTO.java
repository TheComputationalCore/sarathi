package com.busbooking.bus_booking_system.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BookingResponseDTO {

    // =========================
    // Booking Info
    // =========================
    private final Long bookingId;
    private final String status;
    private final LocalDateTime bookingTime;

    // =========================
    // User Info
    // =========================
    private final String userEmail;

    // =========================
    // Bus Info
    // =========================
    private final Long busId;
    private final String fromLocation;
    private final String toLocation;
    private final LocalDateTime departureTime;
    private final LocalDateTime arrivalTime;
    private final double price;
    private final int totalSeats;

    // =========================
    // Travel Info
    // =========================
    private final LocalDate travelDate;
    private final List<String> seatNumbers;

    // =========================
    // Payment Info
    // =========================
    private final Double amountPaid;
    private final String paymentStatus;

    public BookingResponseDTO(
            Long bookingId,
            String status,
            LocalDateTime bookingTime,
            String userEmail,
            Long busId,
            String fromLocation,
            String toLocation,
            LocalDateTime departureTime,
            LocalDateTime arrivalTime,
            double price,
            int totalSeats,
            LocalDate travelDate,
            List<String> seatNumbers,
            Double amountPaid,
            String paymentStatus
    ) {
        this.bookingId = bookingId;
        this.status = status;
        this.bookingTime = bookingTime;
        this.userEmail = userEmail;
        this.busId = busId;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
        this.totalSeats = totalSeats;
        this.travelDate = travelDate;
        this.seatNumbers = seatNumbers;
        this.amountPaid = amountPaid;
        this.paymentStatus = paymentStatus;
    }

    // =========================
    // Getters
    // =========================

    public Long getBookingId() {
        return bookingId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public Long getBusId() {
        return busId;
    }

    public String getFromLocation() {
        return fromLocation;
    }

    public String getToLocation() {
        return toLocation;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public double getPrice() {
        return price;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
}
