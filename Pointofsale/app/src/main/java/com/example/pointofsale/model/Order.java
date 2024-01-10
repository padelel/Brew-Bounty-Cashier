package com.example.pointofsale.model;

import java.util.List;

public class Order {
    private String id, customerName, orderId;
    private List<CartItem> cartItems;

    // Konstruktor tanpa argumen
    public Order() {
        // Kosongkan atau isi dengan nilai default jika diperlukan
    }

    // Konstruktor dengan argumen
    public Order(String customerName, String orderId, List<CartItem> cartItems) {
        this.customerName = customerName;
        this.orderId = orderId;
        this.cartItems = cartItems;
    }

    // ... (getter and setter methods)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
}
