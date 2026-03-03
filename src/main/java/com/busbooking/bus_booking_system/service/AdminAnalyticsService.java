package com.busbooking.bus_booking_system.service;

import com.busbooking.bus_booking_system.dto.response.AdminDashboardResponse;
import com.busbooking.bus_booking_system.repository.BookingRepository;
import com.busbooking.bus_booking_system.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminAnalyticsService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    public AdminAnalyticsService(BookingRepository bookingRepository,
                                 PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
    }

    public AdminDashboardResponse getDashboardStats() {

        // ================= BOOKING STATS =================
        long totalBookings = bookingRepository.count();
        long confirmed = bookingRepository.countByStatus("CONFIRMED");
        long cancelled = bookingRepository.countByStatus("CANCELLED");

        // ================= REVENUE STATS =================
        Long totalRevenuePaise = paymentRepository.getTotalRevenue();

        LocalDateTime startOfDay =
                LocalDateTime.now().toLocalDate().atStartOfDay();

        Long todayRevenuePaise =
                paymentRepository.getTodayRevenue(startOfDay);

        double totalRevenue = (totalRevenuePaise != null ? totalRevenuePaise : 0) / 100.0;
        double todayRevenue = (todayRevenuePaise != null ? todayRevenuePaise : 0) / 100.0;

        return new AdminDashboardResponse(
                totalBookings,
                confirmed,
                cancelled,
                totalRevenue,
                todayRevenue
        );
    }
}
