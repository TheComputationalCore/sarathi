package com.busbooking.bus_booking_system.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PassengerRequest {
    @NotBlank(message = "Passenger name is required")
    @Size(max = 120, message = "Passenger name is too long")
    private String name;

    @Min(value = 1, message = "Passenger age must be at least 1")
    @Max(value = 120, message = "Passenger age must be realistic")
    private int age;

    @NotBlank(message = "Seat number is required")
    private String seatNumber;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
}
