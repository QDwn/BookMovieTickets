package com.example.movieee.Model;

public class ComboBN {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private int price;
    private int quantity;

    public ComboBN() {
        // Default constructor for Firebase
    }

    public ComboBN(String id, String name, String description, String imageUrl, int price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = 0;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
