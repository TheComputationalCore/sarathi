package com.busbooking.bus_booking_system.service;

import com.busbooking.bus_booking_system.dto.LiveBookingEvent;
import com.busbooking.bus_booking_system.dto.response.PaymentOrderResponse;
import com.busbooking.bus_booking_system.entity.Booking;
import com.busbooking.bus_booking_system.entity.Passenger;
import com.busbooking.bus_booking_system.entity.Payment;
import com.busbooking.bus_booking_system.exception.ResourceNotFoundException;
import com.busbooking.bus_booking_system.exception.UnauthorizedActionException;
import com.busbooking.bus_booking_system.repository.BookingRepository;
import com.busbooking.bus_booking_system.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MetricsService metricsService;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${app.payment.mock.enabled:false}")
    private boolean mockPaymentEnabled;

    public PaymentService(BookingRepository bookingRepository,
                          PaymentRepository paymentRepository,
                          SimpMessagingTemplate messagingTemplate,
                          MetricsService metricsService) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.messagingTemplate = messagingTemplate;
        this.metricsService = metricsService;
    }

    // =========================================================
    // CREATE RAZORPAY ORDER (RESILIENT + OBSERVABLE)
    // =========================================================

    @Transactional
    public PaymentOrderResponse createOrder(Long bookingId, String username) {

        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        validateBookingOwnership(booking, username);

        if (!"PAYMENT_PENDING".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException("Booking is not eligible for payment");
        }

        Optional<Payment> existingPayment = paymentRepository.findByBookingId(bookingId);

        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();

            if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
                throw new IllegalStateException("Booking already paid");
            }

            if (!"FAILED".equalsIgnoreCase(payment.getStatus())) {
                return new PaymentOrderResponse(
                        payment.getRazorpayOrderId(),
                        payment.getAmount(),
                        "INR",
                        keyId
                );
            }
        }

        int amountInPaise = calculateBookingAmount(booking);

        final String orderId;
        if (mockPaymentEnabled) {
            orderId = generateMockOrderId(bookingId);
            logger.info("Mock payment order created for booking {}", bookingId);
        } else {
            try {
                orderId = createRazorpayOrderWithResilience(bookingId, amountInPaise);
            } catch (RuntimeException ex) {
                logger.error("Payment order creation failed for booking {}", bookingId, ex);
                throw new IllegalStateException("Payment gateway unavailable. Please retry.");
            }
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setRazorpayOrderId(orderId);
        payment.setAmount(amountInPaise);
        payment.setStatus("CREATED");

        paymentRepository.save(payment);

        booking.setRazorpayOrderId(orderId);
        bookingRepository.save(booking);

        logger.info("Razorpay order created for booking {}", bookingId);

        String responseKey = mockPaymentEnabled ? "mock_key_local" : keyId;
        return new PaymentOrderResponse(orderId, amountInPaise, "INR", responseKey);
    }

    // =========================================================
    // RESILIENT RAZORPAY ORDER CREATION
    // =========================================================

    @CircuitBreaker(name = "razorpayService", fallbackMethod = "orderCreationFallback")
    @Retry(name = "razorpayService")
    public String createRazorpayOrderWithResilience(Long bookingId, int amountInPaise) {

        try {
            RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "booking_" + bookingId);
            orderRequest.put("payment_capture", 1);

            Order order = razorpay.orders.create(orderRequest);

            return order.get("id");

        } catch (Exception e) {
            throw new RuntimeException("Razorpay order creation failed", e);
        }
    }

    public String orderCreationFallback(Long bookingId, int amountInPaise, Throwable ex) {
        logger.error("Razorpay service unavailable for booking {}", bookingId, ex);
        throw new RuntimeException("Payment service temporarily unavailable. Please try again.");
    }

    // =========================================================
    // VERIFY & CONFIRM PAYMENT (LOCKED + METRICS)
    // =========================================================

    @Transactional(dontRollbackOn = IllegalArgumentException.class)
    public void verifyAndConfirmPayment(
            Long bookingId,
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature,
            String username
    ) {

        Booking booking = bookingRepository
                .findByIdWithDetailsForUpdate(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        validateBookingOwnership(booking, username);

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found"));

        if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            logger.info("Duplicate payment confirmation ignored for booking {}", bookingId);
            return;
        }

        boolean isMockOrder = isMockOrderId(razorpayOrderId) || isMockOrderId(payment.getRazorpayOrderId());
        if (!isMockOrder && !verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature)) {
            payment.setStatus("FAILED");
            booking.setStatus("PAYMENT_FAILED");
            paymentRepository.save(payment);
            bookingRepository.save(booking);

            metricsService.incrementPaymentFailed();

            throw new IllegalArgumentException("Invalid payment signature");
        }

        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setStatus("SUCCESS");
        payment.setPaymentTime(LocalDateTime.now());
        paymentRepository.save(payment);

        booking.setStatus("CONFIRMED");
        booking.setRazorpayPaymentId(razorpayPaymentId);
        booking.setPaymentTime(LocalDateTime.now());
        bookingRepository.save(booking);

        metricsService.incrementPaymentSuccess();
        metricsService.incrementBookingConfirmed();

        logger.info("Payment confirmed for booking {}", bookingId);

        broadcastSeatConfirmation(booking);
        broadcastLiveEvent(booking);
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void validateBookingOwnership(Booking booking, String username) {
        if (!booking.getUser().getEmail().equals(username)) {
            throw new UnauthorizedActionException("Not authorized");
        }
    }

    private int calculateBookingAmount(Booking booking) {
        double pricePerSeat = booking.getBus().getPrice();
        int passengerCount = booking.getPassengers() == null
                ? 0
                : booking.getPassengers().size();
        return (int) (pricePerSeat * passengerCount * 100);
    }

    private void broadcastSeatConfirmation(Booking booking) {
        List<String> confirmedSeats = booking.getPassengers()
                .stream()
                .map(Passenger::getSeatNumber)
                .collect(Collectors.toList());

        String travelDate = booking.getPassengers()
                .get(0)
                .getBusSchedule()
                .getTravelDate()
                .toString();

        HashMap<String, Object> seatEvent = new HashMap<>();
        seatEvent.put("busId", booking.getBus().getId());
        seatEvent.put("travelDate", travelDate);
        seatEvent.put("seatNumbers", confirmedSeats);
        seatEvent.put("action", "BOOKED");

        messagingTemplate.convertAndSend("/topic/seat-updates", seatEvent);
    }

    private void broadcastLiveEvent(Booking booking) {
        LiveBookingEvent liveEvent = new LiveBookingEvent(
                "A traveler just joined the journey!",
                booking.getId(),
                booking.getBus().getId(),
                booking.getBus().getFromLocation(),
                booking.getBus().getToLocation()
        );

        messagingTemplate.convertAndSend("/topic/live-bookings", liveEvent);
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey =
                    new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

            mac.init(secretKey);

            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = bytesToHex(hash);

            return generatedSignature.equals(signature);

        } catch (Exception e) {
            logger.error("Error verifying Razorpay signature", e);
            throw new RuntimeException("Signature verification failed");
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == 1) hex.append('0');
            hex.append(s);
        }
        return hex.toString();
    }

    private String generateMockOrderId(Long bookingId) {
        return "mock_order_" + bookingId + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    private boolean isMockOrderId(String orderId) {
        return orderId != null && orderId.startsWith("mock_order_");
    }
}
