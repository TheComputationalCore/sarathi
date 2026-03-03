package com.busbooking.bus_booking_system.dto.response;

public class PaymentOrderResponse {

    private String orderId;
    private int amount;
    private String currency;
    private String key;

    public PaymentOrderResponse(String orderId, int amount, String currency, String key) {
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.key = key;
    }

    public String getOrderId() {
        return orderId;
    }

    public int getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getKey() {
        return key;
    }
}
