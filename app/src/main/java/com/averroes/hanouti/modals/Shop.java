package com.averroes.hanouti.modals;

public class Shop {

    private String uid,
            fullname,
            shop_name,
            phone,
            delivery_fee,
            dayra,
            baladiya,
            address,
            email,
            password,
            account_type,
            timestamp,
            online,
            shop_open,
            profile_image;

    public Shop() {}

    public Shop(String uid, String fullname, String shop_name, String phone, String delivery_fee, String dayra, String baladiya, String address, String email, String password, String account_type, String timestamp, String online, String shop_open, String profile_image) {
        this.uid = uid;
        this.fullname = fullname;
        this.shop_name = shop_name;
        this.phone = phone;
        this.delivery_fee = delivery_fee;
        this.dayra = dayra;
        this.baladiya = baladiya;
        this.address = address;
        this.email = email;
        this.password = password;
        this.account_type = account_type;
        this.timestamp = timestamp;
        this.online = online;
        this.shop_open = shop_open;
        this.profile_image = profile_image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getShop_name() {
        return shop_name;
    }

    public void setShop_name(String shop_name) {
        this.shop_name = shop_name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDelivery_fee() {
        return delivery_fee;
    }

    public void setDelivery_fee(String delivery_fee) {
        this.delivery_fee = delivery_fee;
    }

    public String getDayra() {
        return dayra;
    }

    public void setDayra(String dayra) {
        this.dayra = dayra;
    }

    public String getBaladiya() {
        return baladiya;
    }

    public void setBaladiya(String baladiya) {
        this.baladiya = baladiya;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccount_type() {
        return account_type;
    }

    public void setAccount_type(String account_type) {
        this.account_type = account_type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getShop_open() {
        return shop_open;
    }

    public void setShop_open(String shop_open) {
        this.shop_open = shop_open;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }
}
