package com.busbooking.bus_booking_system.service;

import com.busbooking.bus_booking_system.controller.BookingRequest;
import com.busbooking.bus_booking_system.controller.PassengerRequest;
import com.busbooking.bus_booking_system.entity.*;
import com.busbooking.bus_booking_system.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.busbooking.bus_booking_system.IntegrationTestBase;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest extends IntegrationTestBase{

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusRepository busRepository;

    @Mock
    private BusScheduleRepository busScheduleRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private SeatLockService seatLockService;

    @Mock
    private MetricsService metricsService;

    @Test
    void testCreateBooking_success() {

        // ================= USER =================
        User user = new User();
        user.setEmail("testuser");
        ReflectionTestUtils.setField(user, "id", 1L);

        // ================= BUS =================
        Bus bus = new Bus();
        ReflectionTestUtils.setField(bus, "id", 1L);
        bus.setTotalSeats(10);

        // ================= SCHEDULE =================
        BusSchedule schedule = new BusSchedule();
        schedule.setBus(bus);
        schedule.setTravelDate(LocalDate.now());
        schedule.setAvailableSeats(10);

        // ================= PASSENGER =================
        PassengerRequest passenger = new PassengerRequest();
        passenger.setName("John");
        passenger.setAge(25);
        passenger.setSeatNumber("1");

        // ================= BOOKING REQUEST =================
        BookingRequest request = new BookingRequest();
        request.setBusId(1L);
        request.setTravelDate(LocalDate.now());
        request.setPassengers(List.of(passenger));
        request.setIdempotencyKey("idem-key-123");

        // ================= MOCKS =================

        when(userRepository.findByEmail("testuser"))
                .thenReturn(Optional.of(user));

        when(busRepository.findById(1L))
                .thenReturn(Optional.of(bus));

        when(bookingRepository.findByIdempotencyKey("idem-key-123"))
                .thenReturn(Optional.empty());

        when(busScheduleRepository.findByBusIdAndTravelDateForUpdate(anyLong(), any()))
                .thenReturn(Optional.of(schedule));

        when(seatLockService.isLockedByUser(anyLong(), anyString(), anyString(), anyLong()))
                .thenReturn(true);

        when(bookingRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        when(passengerRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        when(busScheduleRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        doNothing().when(seatLockService)
                .releaseSeatIfOwned(anyLong(), anyString(), anyString(), anyLong());

        // ================= ACT =================
        Booking booking = bookingService.createBooking(request, "testuser");

        // ================= ASSERT =================
        assertNotNull(booking);
    }
}
