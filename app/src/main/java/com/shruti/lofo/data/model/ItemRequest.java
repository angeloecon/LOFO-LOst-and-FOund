package com.shruti.lofo.data.model;


// To create new Item and Store it on the database, A blueprint to create new
public class ItemRequest {
    private String item_name;
    private String description;
    private String location;
    private String date;
    private String time;
    private String category;
    private String type;
    private String image_url;
    private String contact_phone;

    public ItemRequest(String item_name, String description, String location, String date, String time, String category, String type, String image_url, String contact_phone){
        this.item_name = item_name;
        this.description = description;
        this.location = location;
        this.date = date;
        this.time = time;
        this.category = category;
        this.type = type;
        this.image_url = image_url;
        this.contact_phone = contact_phone;
    }
}
