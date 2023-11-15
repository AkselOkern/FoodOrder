package com.example.foodorder;

public class Pizza {

    private String imagePath;
    private String itemName;
    private double price;
    private String size;


    // Required default constructor for Firestore
    public Pizza() {
    }

    public Pizza(String imagePath, String itemName, double price) {
        this.imagePath = imagePath;
        this.itemName = itemName;
        this.price = price;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
