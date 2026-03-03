package com.busbooking.bus_booking_system.controller;

import com.busbooking.bus_booking_system.dto.request.PaymentVerificationRequest;
import com.busbooking.bus_booking_system.dto.response.PaymentOrderResponse;
import com.busbooking.bus_booking_system.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // =========================================================
    // CREATE RAZORPAY ORDER
    // =========================================================

    @PostMapping("/create-order/{bookingId}")
    public ResponseEntity<PaymentOrderResponse> createOrder(
            @PathVariable Long bookingId,
            Authentication authentication
    ) {

        PaymentOrderResponse response =
                paymentService.createOrder(bookingId, authentication.getName());

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // VERIFY PAYMENT (AFTER RAZORPAY SUCCESS)
    // =========================================================

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest request,
            Authentication authentication
    ) {

        paymentService.verifyAndConfirmPayment(
                request.getBookingId(),
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                authentication.getName()
        );

        return ResponseEntity.ok().body(
                java.util.Map.of(
                        "message", "Payment verified successfully",
                        "bookingId", request.getBookingId(),
                        "status", "CONFIRMED"
                )
        );
    }
}
