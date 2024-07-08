package com.example.pointofsale.model;

import java.util.List;

public class Order {
    private String customerName;
    private String orderId;
    private List<CartItem> cartItems; // Menggunakan List<CartItem> di sini
    private double totalPrice;

    public Order() {
        // Diperlukan untuk Firebase
    }

    public Order(String customerName, String orderId, List<CartItem> cartItems, double totalPrice) {
        this.customerName = customerName;
        this.orderId = orderId;
        this.cartItems = cartItems;
        this.totalPrice = totalPrice;
    }

    // Getter dan Setter (atau gunakan @PropertyName di Firebase Realtime Database)

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

    // Inner class Order.CartItem untuk menyesuaikan dengan struktur data di Firebase
    public static class CartItem {
        private String menu;
        private int kuantitas;
        private double harga;

        public CartItem() {
            // Diperlukan untuk Firebase
        }

        public CartItem(String menu, int kuantitas, double harga) {
            this.menu = menu;
            this.kuantitas = kuantitas;
            this.harga = harga;
        }

        // Getter dan Setter (atau gunakan @PropertyName di Firebase Realtime Database)

        public String getMenu() {
            return menu;
        }

        public void setMenu(String menu) {
            this.menu = menu;
        }

        public int getKuantitas() {
            return kuantitas;
        }

        public void setKuantitas(int kuantitas) {
            this.kuantitas = kuantitas;
        }

        public double getHarga() {
            return harga;
        }

        public void setHarga(double harga) {
            this.harga = harga;
        }
    }
}
