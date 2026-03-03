package com.busbooking.bus_booking_system.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final Counter bookingsCreated;
    private final Counter bookingsConfirmed;
    private final Counter bookingsCancelled;

    private final Counter paymentsSuccess;
    private final Counter paymentsFailed;

    private final Counter seatLockSuccess;
    private final Counter seatLockFailure;

    public MetricsService(MeterRegistry registry) {

        this.bookingsCreated = registry.counter("sarathi.bookings.created");
        this.bookingsConfirmed = registry.counter("sarathi.bookings.confirmed");
        this.bookingsCancelled = registry.counter("sarathi.bookings.cancelled");

        this.paymentsSuccess = registry.counter("sarathi.payments.success");
        this.paymentsFailed = registry.counter("sarathi.payments.failed");

        this.seatLockSuccess = registry.counter("sarathi.seat.lock.success");
        this.seatLockFailure = registry.counter("sarathi.seat.lock.failure");
    }

    public void incrementBookingCreated() {
        bookingsCreated.increment();
    }

    public void incrementBookingConfirmed() {
        bookingsConfirmed.increment();
    }

    public void incrementBookingCancelled() {
        bookingsCancelled.increment();
    }

    public void incrementPaymentSuccess() {
        paymentsSuccess.increment();
    }

    public void incrementPaymentFailed() {
        paymentsFailed.increment();
    }

    public void incrementSeatLockSuccess() {
        seatLockSuccess.increment();
    }

    public void incrementSeatLockFailure() {
        seatLockFailure.increment();
    }
}