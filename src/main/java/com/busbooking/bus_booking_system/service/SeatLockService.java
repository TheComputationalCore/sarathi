package com.busbooking.bus_booking_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class SeatLockService {

    private static final Logger logger = LoggerFactory.getLogger(SeatLockService.class);

    private final StringRedisTemplate redisTemplate;
    private final MetricsService metricsService;

    private final Duration lockTtl;

    public SeatLockService(StringRedisTemplate redisTemplate,
                           MetricsService metricsService,
                           @Value("${seat.lock.ttl.minutes:5}") long ttlMinutes) {
        this.redisTemplate = redisTemplate;
        this.metricsService = metricsService;
        this.lockTtl = Duration.ofMinutes(ttlMinutes);
    }

    /*
        Redis Key Format:
        seat:lock:{busId}:{date}:{seatNumber}
     */
    private String buildKey(Long busId, String date, String seatNumber) {
        return "seat:lock:" + busId + ":" + date + ":" + seatNumber;
    }

    // =====================================================
    // SINGLE SEAT LOCK
    // =====================================================

    public boolean lockSeat(Long busId, String date, String seatNumber, Long userId) {

        String key = buildKey(busId, date, seatNumber);

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, String.valueOf(userId), lockTtl);

        if (Boolean.TRUE.equals(success)) {
            metricsService.incrementSeatLockSuccess();
            logger.debug("Seat locked: {}", key);
            return true;
        } else {
            metricsService.incrementSeatLockFailure();
            logger.warn("Seat lock failed: {}", key);
            return false;
        }
    }

    // =====================================================
    // MULTI SEAT LOCK (SAFE ROLLBACK)
    // =====================================================

    public boolean lockSeats(Long busId, String date,
                             List<String> seatNumbers, Long userId) {

        for (String seat : seatNumbers) {

            boolean locked = lockSeat(busId, date, seat, userId);

            if (!locked) {

                logger.warn("Batch lock failed. Rolling back seats for user {}", userId);

                releaseSeatsIfOwned(busId, date, seatNumbers, userId);

                return false;
            }
        }

        return true;
    }

    // =====================================================
    // LOCK CHECKS
    // =====================================================

    public boolean isSeatLocked(Long busId, String date, String seatNumber) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(buildKey(busId, date, seatNumber))
        );
    }

    public boolean isLockedByUser(Long busId, String date,
                                  String seatNumber, Long userId) {

        String value = redisTemplate.opsForValue()
                .get(buildKey(busId, date, seatNumber));

        return String.valueOf(userId).equals(value);
    }

    // =====================================================
    // RELEASE METHODS
    // =====================================================

    public void releaseSeat(Long busId, String date, String seatNumber) {
        redisTemplate.delete(buildKey(busId, date, seatNumber));
    }

    public void releaseSeatIfOwned(Long busId, String date,
                                   String seatNumber, Long userId) {

        String key = buildKey(busId, date, seatNumber);
        String value = redisTemplate.opsForValue().get(key);

        if (String.valueOf(userId).equals(value)) {
            redisTemplate.delete(key);
            logger.debug("Seat released: {}", key);
        }
    }

    public void releaseSeatsIfOwned(Long busId, String date,
                                    List<String> seatNumbers, Long userId) {

        for (String seat : seatNumbers) {
            releaseSeatIfOwned(busId, date, seat, userId);
        }
    }
}