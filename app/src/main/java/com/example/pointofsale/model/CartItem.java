package com.example.pointofsale.model;

public class CartItem {
    private String id; // Tambahkan atribut id
    private String menu;
    private int quantity;
    private int unitPrice;
    private String imageURL;

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

    public String getImageURL(){return imageURL;}

    public void setImageURL(String imageURL) {this.imageURL = imageURL;}
}
