package com.busbooking.bus_booking_system.controller;

import com.busbooking.bus_booking_system.entity.Role;
import com.busbooking.bus_booking_system.entity.User;
import com.busbooking.bus_booking_system.repository.UserRepository;
import com.busbooking.bus_booking_system.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ================= REGISTER =================

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_USER);

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    // ================= LOGIN =================

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        final Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(userDetails);

        // Fetch role from DB
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(new JwtResponse(jwt, user.getRole().name()));
    }
}

// ================= DTOs =================

class LoginRequest {

    private String email;
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

class JwtResponse {

    private final String token;
    private final String role;

    public JwtResponse(String token, String role) {
        this.token = token;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getRole() { return role; }
}
