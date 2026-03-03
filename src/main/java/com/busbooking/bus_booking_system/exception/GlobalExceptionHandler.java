package com.busbooking.bus_booking_system.exception;

import com.busbooking.bus_booking_system.dto.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ===============================
    // 404
    // ===============================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {

        log.warn("Resource not found at {} : {}",
                request.getRequestURI(),
                ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(ex.getMessage()));
    }

    // ===============================
    // 409
    // ===============================

    @ExceptionHandler(SeatAlreadyBookedException.class)
    public ResponseEntity<ApiResponse<Void>> handleSeatConflict(
            SeatAlreadyBookedException ex,
            HttpServletRequest request
    ) {

        log.warn("Seat conflict at {} : {}",
                request.getRequestURI(),
                ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(ex.getMessage()));
    }

    // ===============================
    // 403
    // ===============================

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            UnauthorizedActionException ex,
            HttpServletRequest request
    ) {

        log.warn("Unauthorized action at {} : {}",
                request.getRequestURI(),
                ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure(ex.getMessage()));
    }

    // ===============================
    // 400 - Validation
    // ===============================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        String message =
                ex.getBindingResult().getFieldError() != null
                        ? ex.getBindingResult()
                            .getFieldError()
                            .getDefaultMessage()
                        : "Validation failed";

        log.warn("Validation error at {} : {}",
                request.getRequestURI(),
                message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(message));
    }

    // ===============================
    // 400 - IllegalArgument
    // ===============================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {

        log.warn("Bad request at {} : {}",
                request.getRequestURI(),
                ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(ex.getMessage()));
    }

    // ===============================
    // 503 - Temporary downstream failure
    // ===============================

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request
    ) {

        log.warn("Service temporarily unavailable at {} : {}",
                request.getRequestURI(),
                ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.failure(ex.getMessage()));
    }

    // ===============================
    // 401 - Authentication failure
    // ===============================

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthFailure(
            AuthenticationException ex,
            HttpServletRequest request
    ) {

        boolean isAdminPath = request.getRequestURI() != null
                && request.getRequestURI().startsWith("/api/admin");

        log.warn("Authentication failed at {} : {}",
                request.getRequestURI(),
                ex.getMessage());

        return ResponseEntity
                .status(isAdminPath
                        ? HttpStatus.FORBIDDEN
                        : HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failure("Invalid credentials"));
    }

    // ===============================
    // 500 - All other unexpected errors
    // ===============================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error("Unexpected error at {} : {}",
                request.getRequestURI(),
                ex.getMessage(),
                ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("Internal server error"));
    }
}
