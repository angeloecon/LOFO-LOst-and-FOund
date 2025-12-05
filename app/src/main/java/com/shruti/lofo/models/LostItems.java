package com.shruti.lofo.models;

public class LostItems {

    // --- Item Details (Used for filtering and display) ---
    private String itemName;
    private String category;
    private String dateLost;
    private String timeLost;
    private String location;
    private String description;
    private String imageURI; // URL link from Cloudinary

    // --- User/Owner Details (For contact and tracking) ---
    private String ownerName;
    private Long phnum; // Use Long (capital L) for null safety and Firestore compatibility
    private String email;
    private String userId; // Firebase User ID of the poster

    /**
     * Required public empty constructor for Firebase Firestore.
     * Firestore uses this constructor to create a LostItems object
     * when retrieving data from the database (deserialization).
     */
    public LostItems() {
    }

    // ==========================================================
    //                        GETTERS AND SETTERS
    // ==========================================================

    // --- Item Details ---

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDateLost() {
        return dateLost;
    }

    public void setDateLost(String dateLost) {
        this.dateLost = dateLost;
    }

    public String getTimeLost() {
        return timeLost;
    }

    public void setTimeLost(String timeLost) {
        this.timeLost = timeLost;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }

    // --- User/Owner Details ---

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Long getPhnum() {
        return phnum;
    }

    public void setPhnum(Long phnum) {
        this.phnum = phnum;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}