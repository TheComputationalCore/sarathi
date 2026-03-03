package com.busbooking.bus_booking_system.service;

import com.busbooking.bus_booking_system.dto.response.AdminUserResponseDTO;
import com.busbooking.bus_booking_system.entity.Booking;
import com.busbooking.bus_booking_system.entity.Bus;
import com.busbooking.bus_booking_system.entity.BusSchedule;
import com.busbooking.bus_booking_system.entity.User;
import com.busbooking.bus_booking_system.exception.ResourceNotFoundException;
import com.busbooking.bus_booking_system.repository.BookingRepository;
import com.busbooking.bus_booking_system.repository.BusRepository;
import com.busbooking.bus_booking_system.repository.BusScheduleRepository;
import com.busbooking.bus_booking_system.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService {

    private final BusRepository busRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BusScheduleRepository busScheduleRepository;

    public AdminService(BusRepository busRepository,
                        BookingRepository bookingRepository,
                        UserRepository userRepository,
                        BusScheduleRepository busScheduleRepository) {
        this.busRepository = busRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.busScheduleRepository = busScheduleRepository;
    }

    // =====================================================
    // BUS MANAGEMENT
    // =====================================================

    public Bus addBus(Bus bus) {
        return busRepository.save(bus);
    }

    public Bus updateBus(Long busId, Bus updatedBus) {

        Bus existingBus = busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));

        existingBus.setFromLocation(updatedBus.getFromLocation());
        existingBus.setToLocation(updatedBus.getToLocation());
        existingBus.setDepartureTime(updatedBus.getDepartureTime());
        existingBus.setArrivalTime(updatedBus.getArrivalTime());
        existingBus.setPrice(updatedBus.getPrice());
        existingBus.setTotalSeats(updatedBus.getTotalSeats());

        return busRepository.save(existingBus);
    }

    public void deleteBus(Long busId) {

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));

        busRepository.delete(bus);
    }

    // =====================================================
    // BOOKING MANAGEMENT
    // =====================================================

    public Page<Booking> getAllBookings(Pageable pageable) {
        return bookingRepository.findAllBookings(pageable);
    }

    @Transactional
    public void cancelAnyBooking(Long bookingId) {

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
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

        LocalDate travelDate = schedule.getTravelDate();
        Long busId = schedule.getBus().getId();

        // Lock schedule row
        BusSchedule lockedSchedule = busScheduleRepository
                .findByBusIdAndTravelDateForUpdate(busId, travelDate)
                .orElseThrow(() -> new IllegalStateException("Schedule not found"));

        int seatsToRestore = booking.getPassengers().size();

        lockedSchedule.setAvailableSeats(
                lockedSchedule.getAvailableSeats() + seatsToRestore
        );

        booking.setStatus("CANCELLED");

        busScheduleRepository.save(lockedSchedule);
        bookingRepository.save(booking);
    }

    // =====================================================
    // ADMIN USERS MANAGEMENT
    // =====================================================

    public Page<AdminUserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAllUsersWithBookingCount(pageable);
    }

    public AdminUserResponseDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        long bookingCount = bookingRepository.countByUser_Id(userId);

        return new AdminUserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                bookingCount
        );
    }

    // =====================================================
    // SYSTEM DASHBOARD STATS
    // =====================================================

    public Map<String, Object> getSystemStats() {

        long totalBuses = busRepository.count();
        long totalBookings = bookingRepository.count();
        long confirmedBookings = bookingRepository.countConfirmedBookings();
        long cancelledBookings = bookingRepository.countCancelledBookings();
        long totalUsers = userRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBuses", totalBuses);
        stats.put("totalBookings", totalBookings);
        stats.put("confirmedBookings", confirmedBookings);
        stats.put("cancelledBookings", cancelledBookings);
        stats.put("totalUsers", totalUsers);

        return stats;
    }
}
