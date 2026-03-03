package com.busbooking.bus_booking_system.service;

import com.busbooking.bus_booking_system.entity.Booking;
import com.busbooking.bus_booking_system.entity.BusSchedule;
import com.busbooking.bus_booking_system.repository.BookingRepository;
import com.busbooking.bus_booking_system.repository.BusScheduleRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(name = "booking.expiry.legacy.enabled", havingValue = "true")
public class BookingExpiryScheduler {

    private static final Logger logger =
            LoggerFactory.getLogger(BookingExpiryScheduler.class);

    private static final int EXPIRY_MINUTES = 10;

    private final BookingRepository bookingRepository;
    private final BusScheduleRepository busScheduleRepository;

    public BookingExpiryScheduler(BookingRepository bookingRepository,
                                   BusScheduleRepository busScheduleRepository) {
        this.bookingRepository = bookingRepository;
        this.busScheduleRepository = busScheduleRepository;
    }

    // Runs every 60 seconds
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expirePendingBookings() {

        LocalDateTime cutoff =
                LocalDateTime.now().minusMinutes(EXPIRY_MINUTES);

        List<Booking> expiredBookings =
                bookingRepository.findExpiredPendingBookings(cutoff);

        if (expiredBookings.isEmpty()) {
            return;
        }

        for (Booking booking : expiredBookings) {

            try {

                // Skip if already handled
                if (!"PAYMENT_PENDING".equalsIgnoreCase(booking.getStatus())) {
                    continue;
                }

                if (booking.getPassengers() == null ||
                        booking.getPassengers().isEmpty()) {

                    booking.setStatus("CANCELLED");
                    continue;
                }

                BusSchedule schedule =
                        booking.getPassengers().get(0).getBusSchedule();

                int seatsToRestore = booking.getPassengers().size();

                schedule.setAvailableSeats(
                        schedule.getAvailableSeats() + seatsToRestore
                );

                booking.setStatus("CANCELLED");

                busScheduleRepository.save(schedule);

                logger.info("Expired booking {} and restored {} seats",
                        booking.getId(), seatsToRestore);

            } catch (Exception e) {

                logger.error("Error expiring booking {}",
                        booking.getId(), e);
            }
        }
    }
}
