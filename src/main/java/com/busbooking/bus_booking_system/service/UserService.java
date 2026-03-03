package com.busbooking.bus_booking_system.service;

import com.busbooking.bus_booking_system.dto.request.UserUpdateRequest;
import com.busbooking.bus_booking_system.dto.response.UserProfileResponse;
import com.busbooking.bus_booking_system.entity.User;
import com.busbooking.bus_booking_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // READ use-case → return DTO
    public UserProfileResponse getUserProfile(String email) {
        logger.info("Fetching profile for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    // WRITE use-case → update entity, return DTO
    public UserProfileResponse updateProfile(String email, UserUpdateRequest request) {
        logger.info("Updating profile for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User savedUser = userRepository.save(user);

        logger.info("Profile updated successfully for user: {}", email);

        return new UserProfileResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );
    }
}
