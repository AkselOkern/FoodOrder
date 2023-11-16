package com.example.foodorder;

public class CartItem {
    private String itemName;
    private String size;
    private int quantity;
    private double itemPrice;


    public CartItem(String itemName, String size, int quantity, double price) {
        this.itemName = itemName;
        this.size = size;
        this.quantity = quantity;
        this.itemPrice = price;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getItemPrice() {
        return itemPrice;
    }
    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
    }
}



