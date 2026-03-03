package com.busbooking.bus_booking_system.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class BookingRequest {

    @NotNull(message = "Bus ID is required")
    private Long busId;

    @NotNull(message = "Travel date is required")
    private LocalDate travelDate;

    @NotEmpty(message = "At least one passenger is required")
    @Valid
    private List<PassengerRequest> passengers;

    // 🔥 IDEMPOTENCY KEY (REQUIRED)
    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

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

    public List<PassengerRequest> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<PassengerRequest> passengers) {
        this.passengers = passengers;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
