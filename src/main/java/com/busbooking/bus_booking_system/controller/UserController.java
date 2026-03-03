package com.busbooking.bus_booking_system.controller;

import com.busbooking.bus_booking_system.dto.common.ApiResponse;
import com.busbooking.bus_booking_system.dto.request.UserUpdateRequest;
import com.busbooking.bus_booking_system.dto.response.UserProfileResponse;
import com.busbooking.bus_booking_system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Standard REST: GET /api/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(
            Authentication authentication
    ) {
        String email = authentication.getName();
        UserProfileResponse profile = userService.getUserProfile(email);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * Legacy support: GET /api/users/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            Authentication authentication
    ) {
        String email = authentication.getName();
        UserProfileResponse profile = userService.getUserProfile(email);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * PUT /api/users/me
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateCurrentUser(
            @Valid @RequestBody UserUpdateRequest updateRequest,
            Authentication authentication
    ) {
        String email = authentication.getName();
        UserProfileResponse updatedProfile =
                userService.updateProfile(email, updateRequest);
        return ResponseEntity.ok(ApiResponse.success(updatedProfile));
    }

    /**
     * Legacy support: PUT /api/users/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserProfile(
            @Valid @RequestBody UserUpdateRequest updateRequest,
            Authentication authentication
    ) {
        String email = authentication.getName();
        UserProfileResponse updatedProfile =
                userService.updateProfile(email, updateRequest);
        return ResponseEntity.ok(ApiResponse.success(updatedProfile));
    }
}
