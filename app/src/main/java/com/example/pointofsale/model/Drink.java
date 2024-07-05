package com.example.pointofsale.model;

public class Drink {
    private String id;
    private String name;
    private String price;
    private String category;
    private String description;
    private String imageUrl;

    public Drink(String name, String price, String category, String description, String imageUrl) {
        this.name = name;
        this.price = price;
        this.category = this.category;
        this.description = this.description;
        this.imageUrl = this.imageUrl;
    }

    public Drink() {
        this.name = "";
        this.price = "";
        this.category = "";
        this.description = "";
        this.imageUrl = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
