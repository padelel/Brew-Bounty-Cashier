package com.example.pointofsale.model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderId;
    private String customerName;
    private List<CartItem> cartItems;
    private double totalPrice;

    public Order() {
        // Default constructor required for Firebase
    }

    public Order(String orderId, String customerName, List<CartItem> cartItems, double totalPrice) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.cartItems = cartItems;
        this.totalPrice = totalPrice;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    // Convert List<CartItem> to List<MenuItem>
    public List<MenuItem> convertToMenuItems() {
        List<MenuItem> menuItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            MenuItem menuItem = new MenuItem(cartItem.getMenu(), cartItem.getHarga(), cartItem.getKuantitas());
            menuItems.add(menuItem);
        }
        return menuItems;
    }

    // Definition of CartItem class
    public static class CartItem {
        private String menu;
        private double harga;
        private int kuantitas;

        public CartItem() {
            // Default constructor required for Firebase
        }

        public CartItem(String menu, double harga, int kuantitas) {
            this.menu = menu;
            this.harga = harga;
            this.kuantitas = kuantitas;
        }

        public String getMenu() {
            return menu;
        }

        public void setMenu(String menu) {
            this.menu = menu;
        }

        public double getHarga() {
            return harga;
        }

        public void setHarga(double harga) {
            this.harga = harga;
        }

        public int getKuantitas() {
            return kuantitas;
        }

        public void setKuantitas(int kuantitas) {
            this.kuantitas = kuantitas;
        }
    }
}
