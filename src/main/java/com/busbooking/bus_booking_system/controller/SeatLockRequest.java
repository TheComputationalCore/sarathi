package com.busbooking.bus_booking_system.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

public class SeatLockRequest {

    @NotNull(message = "Bus ID is required")
    private Long busId;

    @NotNull(message = "Travel date is required")
    private LocalDate travelDate;

    @NotEmpty(message = "At least one seat is required")
    private List<@NotBlank(message = "Seat number cannot be blank") String> seatNumbers;

    public Long getBusId() {
        return busId;
    }

    public void setBusId(Long busId) {
        this.busId = busId;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(List<String> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }
}
