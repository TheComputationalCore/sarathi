package com.busbooking.bus_booking_system.service;

import com.busbooking.bus_booking_system.controller.BookingRequest;
import com.busbooking.bus_booking_system.controller.PassengerRequest;
import com.busbooking.bus_booking_system.dto.response.BookingResponseDTO;
import com.busbooking.bus_booking_system.entity.*;
import com.busbooking.bus_booking_system.exception.ResourceNotFoundException;
import com.busbooking.bus_booking_system.exception.SeatAlreadyBookedException;
import com.busbooking.bus_booking_system.exception.UnauthorizedActionException;
import com.busbooking.bus_booking_system.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BusRepository busRepository;
    private final BusScheduleRepository busScheduleRepository;
    private final PassengerRepository passengerRepository;
    private final SeatLockService seatLockService;
    private final MetricsService metricsService;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            BusRepository busRepository,
            BusScheduleRepository busScheduleRepository,
            PassengerRepository passengerRepository,
            SeatLockService seatLockService,
            MetricsService metricsService
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.busRepository = busRepository;
        this.busScheduleRepository = busScheduleRepository;
        this.passengerRepository = passengerRepository;
        this.seatLockService = seatLockService;
        this.metricsService = metricsService;
    }

    // =====================================================
    // CREATE BOOKING (ELITE RACE-SAFE + IDEMPOTENT + METRICS)
    // =====================================================

    @Transactional
    public Booking createBooking(BookingRequest bookingRequest, String email) {

        if (bookingRequest.getIdempotencyKey() == null ||
                bookingRequest.getIdempotencyKey().isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }

        Optional<Booking> existing =
                bookingRepository.findByIdempotencyKey(bookingRequest.getIdempotencyKey());

        if (existing.isPresent()) {
            logger.info("Returning existing booking for idempotency key: {}",
                    bookingRequest.getIdempotencyKey());
            return bookingRepository.findByIdWithDetails(existing.get().getId())
                    .orElse(existing.get());
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Bus bus = busRepository.findById(bookingRequest.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));

        LocalDate travelDate = bookingRequest.getTravelDate();
        int requestedSeats = bookingRequest.getPassengers().size();

        BusSchedule schedule = getOrCreateLockedSchedule(bus, travelDate);

        if (schedule.getAvailableSeats() < requestedSeats) {
            throw new SeatAlreadyBookedException("Seats sold out");
        }

        validateRedisSeatLocks(bus.getId(), travelDate, bookingRequest, user.getId());

        try {
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setBus(bus);
            booking.setStatus("PAYMENT_PENDING");
            booking.setIdempotencyKey(bookingRequest.getIdempotencyKey());
            booking.setBookingTime(LocalDateTime.now());
            booking.setPassengers(new ArrayList<>());

            booking = bookingRepository.save(booking);

            for (PassengerRequest pr : bookingRequest.getPassengers()) {
                Passenger passenger = new Passenger();
                passenger.setName(pr.getName());
                passenger.setAge(pr.getAge());
                passenger.setSeatNumber(pr.getSeatNumber());
                passenger.setBooking(booking);
                passenger.setBusSchedule(schedule);
                passengerRepository.save(passenger);
                booking.getPassengers().add(passenger);
            }

            schedule.setAvailableSeats(schedule.getAvailableSeats() - requestedSeats);
            busScheduleRepository.save(schedule);

            metricsService.incrementBookingCreated();

            logger.info("Booking created successfully. ID: {}", booking.getId());
            return booking;

        } catch (DataIntegrityViolationException ex) {
            logger.warn("Duplicate idempotency detected for key {}",
                    bookingRequest.getIdempotencyKey());

            Booking fallback = bookingRepository
                    .findByIdempotencyKey(bookingRequest.getIdempotencyKey())
                    .orElseThrow(() ->
                            new IllegalStateException("Idempotent booking retrieval failed"));
            return bookingRepository.findByIdWithDetails(fallback.getId())
                    .orElse(fallback);
        } finally {
            releaseRedisLocks(bus.getId(), travelDate, bookingRequest, user.getId());
        }
    }

    // =====================================================
    // CANCEL BOOKING (LOCKED + METRICS)
    // =====================================================

    @Transactional
    public void cancelBooking(Long bookingId, String email) {

        Booking booking = bookingRepository
                .findByIdWithDetailsForUpdate(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getEmail().equals(email)) {
            throw new UnauthorizedActionException("Not authorized");
        }

        if (!"CONFIRMED".equalsIgnoreCase(booking.getStatus()) &&
                !"PAYMENT_PENDING".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException("Booking cannot be cancelled");
        }

        if (booking.getPassengers() == null || booking.getPassengers().isEmpty()) {
            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);
            metricsService.incrementBookingCancelled();
            return;
        }

        BusSchedule schedule = booking.getPassengers()
                .get(0)
                .getBusSchedule();

        BusSchedule lockedSchedule = busScheduleRepository
                .findByBusIdAndTravelDateForUpdate(
                        schedule.getBus().getId(),
                        schedule.getTravelDate()
                )
                .orElseThrow(() ->
                        new IllegalStateException("Schedule not found"));

        int seatsToRestore = booking.getPassengers().size();

        lockedSchedule.setAvailableSeats(
                lockedSchedule.getAvailableSeats() + seatsToRestore
        );

        busScheduleRepository.save(lockedSchedule);

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        metricsService.incrementBookingCancelled();

        logger.info("Booking {} cancelled safely", bookingId);
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    private BusSchedule getOrCreateLockedSchedule(Bus bus, LocalDate travelDate) {

        Optional<BusSchedule> existing =
                busScheduleRepository.findByBusIdAndTravelDateForUpdate(
                        bus.getId(), travelDate
                );

        if (existing.isPresent()) {
            return existing.get();
        }

        try {
            BusSchedule newSchedule = new BusSchedule();
            newSchedule.setBus(bus);
            newSchedule.setTravelDate(travelDate);
            newSchedule.setAvailableSeats(bus.getTotalSeats());

            return busScheduleRepository.save(newSchedule);

        } catch (DataIntegrityViolationException ex) {
            return busScheduleRepository
                    .findByBusIdAndTravelDateForUpdate(bus.getId(), travelDate)
                    .orElseThrow(() ->
                            new IllegalStateException("Failed to retrieve schedule after race"));
        }
    }

    private void validateRedisSeatLocks(Long busId,
                                        LocalDate travelDate,
                                        BookingRequest request,
                                        Long userId) {

        for (PassengerRequest p : request.getPassengers()) {
            boolean lockedByUser = seatLockService.isLockedByUser(
                    busId,
                    travelDate.toString(),
                    p.getSeatNumber(),
                    userId
            );

            if (!lockedByUser) {
                throw new SeatAlreadyBookedException(
                        "Seat not locked or locked by another user: " + p.getSeatNumber()
                );
            }
        }
    }

    private void releaseRedisLocks(Long busId,
                                   LocalDate travelDate,
                                   BookingRequest request,
                                   Long userId) {

        for (PassengerRequest p : request.getPassengers()) {
            seatLockService.releaseSeatIfOwned(
                    busId,
                    travelDate.toString(),
                    p.getSeatNumber(),
                    userId
            );
        }
    }

    // =====================================================
    // BOOKING HISTORY
    // =====================================================

    @Transactional
    public List<BookingResponseDTO> getBookingHistory(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Booking> bookings = bookingRepository.findByUserId(user.getId());

        List<BookingResponseDTO> response = new ArrayList<>();

        for (Booking booking : bookings) {
            response.add(mapToBookingResponse(booking));
        }

        return response;
    }

    public BookingResponseDTO mapToBookingResponse(Booking booking) {

        List<String> seats = booking.getPassengers() == null
                ? List.of()
                : booking.getPassengers().stream()
                .map(Passenger::getSeatNumber)
                .toList();

        LocalDate travelDate = (booking.getPassengers() == null ||
                booking.getPassengers().isEmpty())
                ? null
                : booking.getPassengers().get(0)
                .getBusSchedule().getTravelDate();

        Double amount = booking.getPayment() == null
                ? null
                : booking.getPayment().getAmount() / 100.0;

        String paymentStatus = booking.getPayment() == null
                ? null
                : booking.getPayment().getStatus();

        return new BookingResponseDTO(
                booking.getId(),
                booking.getStatus(),
                booking.getBookingTime(),
                booking.getUser().getEmail(),
                booking.getBus().getId(),
                booking.getBus().getFromLocation(),
                booking.getBus().getToLocation(),
                booking.getBus().getDepartureTime(),
                booking.getBus().getArrivalTime(),
                booking.getBus().getPrice(),
                booking.getBus().getTotalSeats(),
                travelDate,
                seats,
                amount,
                paymentStatus
        );
    }
}
