package com.example.pointofsale.model;

public class CartItem {
    private String menu;
    private int quantity;
    private int unitPrice;

    // No-argument constructor for Firestore deserialization
    public CartItem() {
        // Needed by Firestore to deserialize
    }

    public CartItem(String menu, int quantity, int unitPrice) {
        this.menu = menu;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getMenu() {
        return menu;
    }

    public int getKuantitas() {
        return quantity;
    }

    public int getHarga() {
        return unitPrice;
    }

    public double getTotalPrice() {
        return quantity * unitPrice;
    }
}
