package com.busbooking.bus_booking_system.security;

import io.github.bucket4j.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(RateLimitFilter.class);

    // 🔥 In-memory bucket storage
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private final Counter rateLimitExceededCounter;

    public RateLimitFilter(MeterRegistry meterRegistry) {
        this.rateLimitExceededCounter =
                meterRegistry.counter("rate.limit.exceeded.total");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!shouldRateLimit(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!isAuthenticated(authentication)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = authentication.getName();
        String bucketKey = buildBucketKey(username, path);

        Bucket bucket = buckets.computeIfAbsent(
                bucketKey,
                key -> Bucket.builder()
                        .addLimit(resolveBandwidth(path))
                        .build()
        );

        ConsumptionProbe probe =
                bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {

            response.setHeader(
                    "X-RateLimit-Remaining",
                    String.valueOf(probe.getRemainingTokens())
            );

            filterChain.doFilter(request, response);
            return;
        }

        rateLimitExceededCounter.increment();

        long waitSeconds =
                Duration.ofNanos(probe.getNanosToWaitForRefill())
                        .toSeconds();

        logger.warn(
                "Rate limit exceeded | user={} | path={} | retryAfter={}s",
                username,
                path,
                waitSeconds
        );

        sendRateLimitResponse(response, waitSeconds);
    }

    // =========================================================
    // AUTH CHECK
    // =========================================================

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    // =========================================================
    // RATE LIMIT STRATEGY
    // =========================================================

    private boolean shouldRateLimit(String path) {
        return path.startsWith("/api/bookings")
                || path.startsWith("/api/seats")
                || path.startsWith("/api/payments")
                || path.startsWith("/api/trails");
    }

    private String buildBucketKey(String username, String path) {

        if (path.startsWith("/api/seats"))
            return "rate:seats:" + username;

        if (path.startsWith("/api/bookings"))
            return "rate:bookings:" + username;

        if (path.startsWith("/api/payments"))
            return "rate:payments:" + username;

        if (path.startsWith("/api/trails"))
            return "rate:trails:" + username;

        return "rate:general:" + username;
    }

    private Bandwidth resolveBandwidth(String path) {

        if (path.startsWith("/api/seats"))
            return Bandwidth.classic(
                    20,
                    Refill.greedy(20, Duration.ofMinutes(1))
            );

        if (path.startsWith("/api/bookings"))
            return Bandwidth.classic(
                    10,
                    Refill.greedy(10, Duration.ofMinutes(1))
            );

        if (path.startsWith("/api/payments"))
            return Bandwidth.classic(
                    10,
                    Refill.greedy(10, Duration.ofMinutes(1))
            );

        if (path.startsWith("/api/trails"))
            return Bandwidth.classic(
                    30,
                    Refill.greedy(30, Duration.ofMinutes(1))
            );

        return Bandwidth.classic(
                40,
                Refill.greedy(40, Duration.ofMinutes(1))
        );
    }

    // =========================================================
    // RESPONSE
    // =========================================================

    private void sendRateLimitResponse(
            HttpServletResponse response,
            long retryAfterSeconds
    ) throws IOException {

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.setHeader("Retry-After",
                String.valueOf(retryAfterSeconds));
        response.setHeader("X-RateLimit-Remaining", "0");

        response.getWriter().write("""
            {
              "error": "Too many requests",
              "message": "Rate limit exceeded. Please try again shortly."
            }
        """);
    }
}