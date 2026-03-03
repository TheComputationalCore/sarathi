package com.busbooking.bus_booking_system.dto.response;

import java.util.List;

public class SeatStatusResponse {

    private List<String> available;
    private List<String> locked;
    private List<String> booked;

    public SeatStatusResponse(List<String> available, List<String> locked, List<String> booked) {
        this.available = available;
        this.locked = locked;
        this.booked = booked;
    }

    public List<String> getAvailable() { return available; }
    public List<String> getLocked() { return locked; }
    public List<String> getBooked() { return booked; }
}
