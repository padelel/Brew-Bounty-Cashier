package com.example.pointofsale.model;

public class CartItem {
    private String id; // Tambahkan atribut id
    private String menu;
    private int quantity;
    private int unitPrice;

    public CartItem(String id, String menu, int quantity, int unitPrice) {
        this.id = id;
        this.menu = menu;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Tambahkan getter dan setter untuk id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMenu() {
        return menu;
    }

    public int getKuantitas() {
        return quantity;
    }

    public void setKuantitas(int quantity) {
        this.quantity = quantity;
    }

    public int getHarga() {
        return unitPrice;
    }

    public double getTotalPrice() {
        return quantity * unitPrice;
    }
}
