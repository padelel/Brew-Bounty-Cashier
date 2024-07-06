package com.example.pointofsale.model;

import java.util.List;

public class Order {
    private String customerName;
    private String orderId;
    private List<CartItem> cartItems;
    private double totalPrice; // New attribute for total price

    public Order(String customerName, String orderId, List<CartItem> cartItems, double totalPrice) {
        this.customerName = customerName;
        this.orderId = orderId;
        this.cartItems = cartItems;
        this.totalPrice = totalPrice;
    }

    // Add getters and setters
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
