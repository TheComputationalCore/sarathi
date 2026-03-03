package com.busbooking.bus_booking_system.service;

import com.busbooking.bus_booking_system.entity.Booking;
import com.busbooking.bus_booking_system.entity.BusSchedule;
import com.busbooking.bus_booking_system.entity.Payment;
import com.busbooking.bus_booking_system.repository.BookingRepository;
import com.busbooking.bus_booking_system.repository.BusScheduleRepository;
import com.busbooking.bus_booking_system.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentReconciliationService {

    private static final Logger logger =
            LoggerFactory.getLogger(PaymentReconciliationService.class);

    private static final int RECONCILE_AFTER_MINUTES = 5;

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BusScheduleRepository busScheduleRepository;

    private final Counter reconcileAttempts;
    private final Counter reconcileSuccess;
    private final Counter reconcileFailure;
    private final Counter reconcileErrors;
    private final Timer reconcileTimer;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    public PaymentReconciliationService(PaymentRepository paymentRepository,
                                        BookingRepository bookingRepository,
                                        BusScheduleRepository busScheduleRepository,
                                        MeterRegistry meterRegistry) {

        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.busScheduleRepository = busScheduleRepository;

        this.reconcileAttempts = meterRegistry.counter("payment_reconcile_attempt_total");
        this.reconcileSuccess = meterRegistry.counter("payment_reconcile_success_total");
        this.reconcileFailure = meterRegistry.counter("payment_reconcile_failed_total");
        this.reconcileErrors = meterRegistry.counter("payment_reconcile_error_total");
        this.reconcileTimer = meterRegistry.timer("payment_reconcile_duration");
    }

    // =========================================================
    // SCHEDULED JOB
    // =========================================================

    @Scheduled(fixedRate = 180000)
    public void reconcilePayments() {

        LocalDateTime cutoff =
                LocalDateTime.now().minusMinutes(RECONCILE_AFTER_MINUTES);

        List<Long> pendingBookingIds =
                bookingRepository.findExpiredPendingBookingIds(cutoff);

        if (pendingBookingIds.isEmpty()) {
            return;
        }

        logger.warn("Reconciling {} pending bookings",
                pendingBookingIds.size());

        for (Long bookingId : pendingBookingIds) {
            reconcileTimer.record(() -> {
                try {
                    reconcileSingleBooking(bookingId);
                } catch (Exception ex) {
                    reconcileErrors.increment();
                    logger.error("Reconciliation failed for booking {}",
                            bookingId, ex);
                }
            });
        }
    }

    // =========================================================
    // CORE RECONCILIATION LOGIC
    // =========================================================

    @Transactional
    protected void reconcileSingleBooking(Long bookingId) {

        reconcileAttempts.increment();

        Booking booking = bookingRepository
                .findByIdWithDetailsForUpdate(bookingId)
                .orElse(null);

        if (booking == null) return;

        if (!"PAYMENT_PENDING".equalsIgnoreCase(booking.getStatus())) {
            return;
        }

        Optional<Payment> paymentOptional =
                paymentRepository.findByBookingId(bookingId);

        if (paymentOptional.isEmpty()) {
            cancelAndRestoreSeats(booking);
            reconcileFailure.increment();
            return;
        }

        Payment payment = paymentOptional.get();

        if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            booking.setStatus("CONFIRMED");
            reconcileSuccess.increment();
            return;
        }

        if (booking.getRazorpayOrderId() == null) {
            cancelAndRestoreSeats(booking);
            payment.setStatus("FAILED");
            reconcileFailure.increment();
            return;
        }

        boolean paid = checkIfOrderPaid(booking.getRazorpayOrderId());

        if (paid) {
            confirmBooking(booking, payment);
            reconcileSuccess.increment();
            logger.info("Booking {} reconciled SUCCESS", bookingId);
        } else {
            cancelAndRestoreSeats(booking);
            payment.setStatus("FAILED");
            reconcileFailure.increment();
            logger.info("Booking {} reconciled FAILED", bookingId);
        }
    }

    // =========================================================
    // RAZORPAY CHECK
    // =========================================================

    @CircuitBreaker(name = "razorpayService", fallbackMethod = "razorpayFallback")
    @Retry(name = "razorpayService")
    public boolean checkIfOrderPaid(String orderId) {

        try {
            RazorpayClient razorpay =
                    new RazorpayClient(keyId, keySecret);

            Order order = razorpay.orders.fetch(orderId);

            String status = order.get("status");

            return "paid".equalsIgnoreCase(status);

        } catch (Exception ex) {
            throw new RuntimeException("Failed to fetch order", ex);
        }
    }

    public boolean razorpayFallback(String orderId, Throwable ex) {
        logger.error("Razorpay unavailable during reconciliation", ex);
        return false;
    }

    // =========================================================
    // CONFIRM
    // =========================================================

    private void confirmBooking(Booking booking, Payment payment) {

        if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            return;
        }

        payment.setStatus("SUCCESS");
        payment.setPaymentTime(LocalDateTime.now());

        booking.setStatus("CONFIRMED");
        booking.setPaymentTime(LocalDateTime.now());
    }

    // =========================================================
    // CANCEL + RESTORE
    // =========================================================

    private void cancelAndRestoreSeats(Booking booking) {

        if (booking.getPassengers() == null ||
                booking.getPassengers().isEmpty()) {

            booking.setStatus("CANCELLED");
            return;
        }

        BusSchedule schedule =
                booking.getPassengers().get(0).getBusSchedule();

        BusSchedule lockedSchedule =
                busScheduleRepository
                        .findByBusIdAndTravelDateForUpdate(
                                schedule.getBus().getId(),
                                schedule.getTravelDate()
                        )
                        .orElseThrow();

        int seats = booking.getPassengers().size();

        lockedSchedule.setAvailableSeats(
                lockedSchedule.getAvailableSeats() + seats
        );

        booking.setStatus("CANCELLED");
    }
}