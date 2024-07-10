package com.example.pointofsale.model;

public class Pesanan {
    private String menu;
    private int harga;
    private int kuantitas;
    private String imageURL;

    public Pesanan(String menu, int harga, int kuantitas) {
        this.menu = menu;
        this.harga = harga;
        this.kuantitas = kuantitas;
    }

    public Pesanan(){
    }

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public int getHarga() {
        return harga;
    }

    public void setHarga(int harga) {
        this.harga = harga;
    }

    public int getKuantitas() {
        return kuantitas;
    }

    public void setKuantitas(int kuantitas) {
        this.kuantitas = kuantitas;
    }

    public String getImageURL(){ return imageURL; }

    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
}