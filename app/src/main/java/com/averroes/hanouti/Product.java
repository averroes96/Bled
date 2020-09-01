package com.averroes.hanouti;

public class Product {

    private String
            product_id,
            title,
            description,
            category,
            quantity,
            price,
            discount_price,
            discount_note,
            timestamp,
            uid,
            product_icon;

    private boolean discount_available;

    public Product(){

    }

    public Product(String product_id, String title, String description, String category, String quantity, String price,
                   String discount_price, String discount_note, boolean discount_available, String timestamp, String uid, String product_icon) {
        this.product_id = product_id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.discount_price = discount_price;
        this.discount_note = discount_note;
        this.discount_available = discount_available;
        this.timestamp = timestamp;
        this.uid = uid;
        this.product_icon = product_icon;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDiscount_price() {
        return discount_price;
    }

    public void setDiscount_price(String discount_price) {
        this.discount_price = discount_price;
    }

    public String getDiscount_note() {
        return discount_note;
    }

    public void setDiscount_note(String discount_note) {
        this.discount_note = discount_note;
    }

    public boolean getDiscount_available() {
        return discount_available;
    }

    public void setDiscount_available(boolean discount_available) {
        this.discount_available = discount_available;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProduct_icon() {
        return product_icon;
    }

    public void setProduct_icon(String product_icon) {
        this.product_icon = product_icon;
    }
}
