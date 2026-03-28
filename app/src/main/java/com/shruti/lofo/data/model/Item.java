package com.shruti.lofo.data.model;

public class Item {
    private int item_id;
    private int user_id;
    private String type;
    private String item_name;
    private String category;
    private String date;
    private String time;
    private String location;
    private String description;
    private String contact_phone;
    private String image_url;
    private String status;
    private String created_at;

    public Item () {}

    // GETTER SECTION __________________________
    public int getItem_id() {
        return item_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getType() {
        return type;
    }

    public String getItem_name() {
        return item_name;
    }

    public String getCategory() {
        return category;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getContact_phone() {
        return contact_phone;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getStatus() {
        return status;
    }

    public String getCreated_at() {
        return created_at;
    }

    // SETTER SECTION __________________________

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setContact_phone(String contact_phone) {
        this.contact_phone = contact_phone;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
