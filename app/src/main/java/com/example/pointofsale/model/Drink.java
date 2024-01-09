package com.example.pointofsale.model;

public class Drink {
    private String id, product, price;

    public Drink (String product, String price){
        this.product = product;
        this.price = price;
    }

    public Drink (){
        this.product = "";
        this.price = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
