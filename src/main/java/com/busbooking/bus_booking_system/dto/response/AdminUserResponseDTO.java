package com.busbooking.bus_booking_system.dto.response;

import com.busbooking.bus_booking_system.entity.Role;

public class AdminUserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private Long bookingCount;

    public AdminUserResponseDTO(
            Long id,
            String name,
            String email,
            Role role,
            Long bookingCount
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.bookingCount = bookingCount;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public Long getBookingCount() { return bookingCount; }
}
