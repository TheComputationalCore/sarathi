package com.busbooking.bus_booking_system.dto;

public class LiveBookingEvent {

    private String message;
    private Long bookingId;
    private Long busId;
    private String from;
    private String to;

    public LiveBookingEvent(String message,
                            Long bookingId,
                            Long busId,
                            String from,
                            String to) {
        this.message = message;
        this.bookingId = bookingId;
        this.busId = busId;
        this.from = from;
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getBusId() {
        return busId;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
