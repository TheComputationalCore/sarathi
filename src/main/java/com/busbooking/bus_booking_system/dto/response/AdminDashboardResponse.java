package com.busbooking.bus_booking_system.dto.response;

public class AdminDashboardResponse {

    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;
    private double totalRevenue;
    private double todayRevenue;

    public AdminDashboardResponse(long totalBookings,
                                  long confirmedBookings,
                                  long cancelledBookings,
                                  double totalRevenue,
                                  double todayRevenue) {
        this.totalBookings = totalBookings;
        this.confirmedBookings = confirmedBookings;
        this.cancelledBookings = cancelledBookings;
        this.totalRevenue = totalRevenue;
        this.todayRevenue = todayRevenue;
    }

    public long getTotalBookings() { return totalBookings; }
    public long getConfirmedBookings() { return confirmedBookings; }
    public long getCancelledBookings() { return cancelledBookings; }
    public double getTotalRevenue() { return totalRevenue; }
    public double getTodayRevenue() { return todayRevenue; }
}
