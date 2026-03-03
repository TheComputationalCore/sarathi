package com.busbooking.bus_booking_system.exception;

public class UnauthorizedActionException extends RuntimeException {

    public UnauthorizedActionException(String message) {
        super(message);
    }
}
