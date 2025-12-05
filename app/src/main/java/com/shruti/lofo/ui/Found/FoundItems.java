package com.shruti.lofo.ui.Found;

public class FoundItems {
    private String itemName;
    private String finderName;
    private String finderId ;
    private String category;
    private String dateFound;
    private String timeFound; // <--- ADDED
    private String location;
    private String email;
    private String phnum;
    private String description;
    private String imageURI;
    private String tag = "Found";

    public FoundItems() {
    }

    // Getters & Setters
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getFinderName() { return finderName; }
    public void setFinderName(String finderName) { this.finderName = finderName; }

    public String getFinderId() { return finderId; }
    public void setFinderId(String finderId) { this.finderId = finderId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDateFound() { return dateFound; }
    public void setDateFound(String dateFound) { this.dateFound = dateFound; }

    public String getTimeFound() { return timeFound; } // <--- ADDED
    public void setTimeFound(String timeFound) { this.timeFound = timeFound; } // <--- ADDED

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhnum() { return phnum; }
    public void setPhnum(String phnum) { this.phnum = phnum; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageURI() { return imageURI; }
    public void setImageURI(String imageURI) { this.imageURI = imageURI; }

    public String getTag() { return tag; }
}