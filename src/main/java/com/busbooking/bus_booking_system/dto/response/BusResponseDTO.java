package com.busbooking.bus_booking_system.dto.response;

import java.time.LocalDateTime;

public class BusResponseDTO {

    private Long id;
    private String fromLocation;
    private String toLocation;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private double price;
    private int totalSeats;

    public BusResponseDTO(Long id,
                          String fromLocation,
                          String toLocation,
                          LocalDateTime departureTime,
                          LocalDateTime arrivalTime,
                          double price,
                          int totalSeats) {
        this.id = id;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
        this.totalSeats = totalSeats;
    }

    public Long getId() { return id; }
    public String getFromLocation() { return fromLocation; }
    public String getToLocation() { return toLocation; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public double getPrice() { return price; }
    public int getTotalSeats() { return totalSeats; }
}
