package com.busbooking.bus_booking_system.service;

import com.busbooking.bus_booking_system.entity.Booking;
import com.busbooking.bus_booking_system.entity.BusSchedule;
import com.busbooking.bus_booking_system.entity.Payment;
import com.busbooking.bus_booking_system.repository.BookingRepository;
import com.busbooking.bus_booking_system.repository.BusScheduleRepository;
import com.busbooking.bus_booking_system.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(BookingCleanupService.class);

    private static final int EXPIRY_MINUTES = 10;

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final BusScheduleRepository busScheduleRepository;

    public BookingCleanupService(BookingRepository bookingRepository,
                                 PaymentRepository paymentRepository,
                                 BusScheduleRepository busScheduleRepository) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.busScheduleRepository = busScheduleRepository;
    }

    /**
     * Runs every 5 minutes.
     * Safely expires bookings stuck in PAYMENT_PENDING state.
     */
    @Scheduled(fixedRate = 300000)
    public void expireUnpaidBookings() {

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(EXPIRY_MINUTES);

        List<Long> expiredBookingIds =
                bookingRepository.findExpiredPendingBookingIds(cutoff);

        if (expiredBookingIds.isEmpty()) {
            return;
        }

        logger.warn("Found {} expired unpaid bookings", expiredBookingIds.size());

        for (Long bookingId : expiredBookingIds) {
            try {
                expireSingleBooking(bookingId);
            } catch (Exception ex) {
                logger.error("Failed to expire booking {}", bookingId, ex);
            }
        }
    }

    /**
     * Expires a single booking in a fully locked and isolated transaction.
     */
    @Transactional
    protected void expireSingleBooking(Long bookingId) {

        Booking booking = bookingRepository
                .findByIdWithDetailsForUpdate(bookingId)
                .orElse(null);

        if (booking == null) {
            return;
        }

        // Re-check inside lock (critical)
        if (!"PAYMENT_PENDING".equalsIgnoreCase(booking.getStatus())) {
            return;
        }

        if (booking.getPassengers() == null || booking.getPassengers().isEmpty()) {
            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);
            return;
        }

        // All passengers belong to same schedule
        BusSchedule schedule = booking.getPassengers()
                .get(0)
                .getBusSchedule();

        Long busId = schedule.getBus().getId();
        LocalDate travelDate = schedule.getTravelDate();

        // Lock schedule row before seat restoration
        BusSchedule lockedSchedule = busScheduleRepository
                .findByBusIdAndTravelDateForUpdate(busId, travelDate)
                .orElseThrow(() ->
                        new IllegalStateException("Schedule not found during cleanup"));

        int seatsToRestore = booking.getPassengers().size();

        lockedSchedule.setAvailableSeats(
                lockedSchedule.getAvailableSeats() + seatsToRestore
        );

        busScheduleRepository.save(lockedSchedule);

        // Update payment if exists
        Payment payment = paymentRepository
                .findByBookingId(bookingId)
                .orElse(null);

        if (payment != null && !"SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            payment.setStatus("FAILED");
            paymentRepository.save(payment);
        }

        // Final booking state change
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        logger.info("Expired booking {} and restored {} seats",
                bookingId, seatsToRestore);
    }
}