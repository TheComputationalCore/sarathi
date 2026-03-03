package com.busbooking.bus_booking_system.controller;

import com.busbooking.bus_booking_system.dto.response.SeatStatusResponse;
import com.busbooking.bus_booking_system.entity.Bus;
import com.busbooking.bus_booking_system.entity.BusSchedule;
import com.busbooking.bus_booking_system.entity.Passenger;
import com.busbooking.bus_booking_system.entity.User;
import com.busbooking.bus_booking_system.repository.BusRepository;
import com.busbooking.bus_booking_system.repository.BusScheduleRepository;
import com.busbooking.bus_booking_system.repository.UserRepository;
import com.busbooking.bus_booking_system.service.SeatLockService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seats")
public class SeatLockController {

    private final SeatLockService seatLockService;
    private final UserRepository userRepository;
    private final BusRepository busRepository;
    private final BusScheduleRepository busScheduleRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public SeatLockController(SeatLockService seatLockService,
                              UserRepository userRepository,
                              BusRepository busRepository,
                              BusScheduleRepository busScheduleRepository,
                              SimpMessagingTemplate messagingTemplate) {
        this.seatLockService = seatLockService;
        this.userRepository = userRepository;
        this.busRepository = busRepository;
        this.busScheduleRepository = busScheduleRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // ================= LOCK SEATS =================

    @PostMapping("/lock")
    public ResponseEntity<?> lockSeats(@Valid @RequestBody SeatLockRequest request,
                                       Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow();

        boolean locked = seatLockService.lockSeats(
                request.getBusId(),
                request.getTravelDate().toString(),
                request.getSeatNumbers(),
                user.getId()
        );

        if (!locked) {
            return ResponseEntity.status(409).body("Some seats already locked");
        }

        // 🔥 BROADCAST LOCK EVENT
        Map<String, Object> event = new HashMap<>();
        event.put("busId", request.getBusId());
        event.put("travelDate", request.getTravelDate().toString());
        event.put("seatNumbers", request.getSeatNumbers());
        event.put("action", "LOCKED");

        messagingTemplate.convertAndSend("/topic/seat-updates", event);

        return ResponseEntity.ok("Seats locked for 5 minutes");
    }

    // ================= RELEASE SEATS =================

    @PostMapping("/release")
    public ResponseEntity<?> releaseSeats(@Valid @RequestBody SeatLockRequest request,
                                          Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow();

        seatLockService.releaseSeatsIfOwned(
                request.getBusId(),
                request.getTravelDate().toString(),
                request.getSeatNumbers(),
                user.getId()
        );

        // 🔥 BROADCAST RELEASE EVENT
        Map<String, Object> event = new HashMap<>();
        event.put("busId", request.getBusId());
        event.put("travelDate", request.getTravelDate().toString());
        event.put("seatNumbers", request.getSeatNumbers());
        event.put("action", "RELEASED");

        messagingTemplate.convertAndSend("/topic/seat-updates", event);

        return ResponseEntity.ok("Seats released");
    }

    // ================= SEAT STATUS =================

    @GetMapping("/status")
    public ResponseEntity<SeatStatusResponse> getSeatStatus(
            @RequestParam Long busId,
            @RequestParam LocalDate travelDate) {

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new RuntimeException("Bus not found"));

        Optional<BusSchedule> scheduleOpt =
                busScheduleRepository.findByBusIdAndTravelDate(busId, travelDate);

        Set<String> bookedSeats = new HashSet<>();

        if (scheduleOpt.isPresent()) {
            BusSchedule schedule = scheduleOpt.get();

            bookedSeats = schedule.getPassengers()
                    .stream()
                    .map(Passenger::getSeatNumber)
                    .collect(Collectors.toSet());
        }

        List<String> allSeats = new ArrayList<>();
        for (int i = 1; i <= bus.getTotalSeats(); i++) {
            allSeats.add(String.valueOf(i));
        }

        List<String> lockedSeats = new ArrayList<>();
        List<String> availableSeats = new ArrayList<>();

        for (String seat : allSeats) {

            if (bookedSeats.contains(seat)) {
                continue;
            }

            boolean locked = seatLockService.isSeatLocked(
                    busId,
                    travelDate.toString(),
                    seat
            );

            if (locked) {
                lockedSeats.add(seat);
            } else {
                availableSeats.add(seat);
            }
        }

        return ResponseEntity.ok(
                new SeatStatusResponse(
                        availableSeats,
                        lockedSeats,
                        new ArrayList<>(bookedSeats)
                )
        );
    }
}
