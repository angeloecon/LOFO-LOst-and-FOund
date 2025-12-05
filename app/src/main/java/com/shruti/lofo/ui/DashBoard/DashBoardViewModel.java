package com.shruti.lofo.ui.DashBoard;

import androidx.lifecycle.ViewModel;

public class DashBoardViewModel extends ViewModel {

    // --- Fields ---
    String imageURI;
    String documentId; // Used to hold the Firestore Document ID
    String category, description, ownerName, finderName, tag, dateLost, dateFound, itemName;
    String collectionName;


    public DashBoardViewModel(String imageURI, String category, String description, String ownerName, String finderName, String tag, String dateLost, String itemName, String dateFound) {
        this.imageURI = imageURI;
        this.category = category;
        this.description = description;
        this.ownerName = ownerName;
        this.finderName = finderName;
        this.itemName = itemName;
        this.tag = tag;
        this.dateLost = dateLost;
        this.dateFound = dateFound;
        // Removed: this.documentId = documentId; // Fix: documentId isn't an argument here
    }

    // --- Empty Constructor ---
    public DashBoardViewModel() {
        // Required empty public constructor for Firestore's .toObject(Class.class)
    }

    // --- Setters (CRITICAL FIXES for DashBoardFragment) ---

    // 1. Missing setter for 'tag'
    public void setTag(String tag) {
        this.tag = tag;
    }

    // 2. Setter for 'documentId'
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // --- Getters ---

    public String getDocumentId() {
        return documentId;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getImageURI() {
        return imageURI;
    }
    public String getCategory() {
        return category;
    }
    public String getDescription() {
        return description;
    }
    public String getOwnerName() {
        return ownerName;
    }
    public String getFinderName() {
        return finderName;
    }
    public String getDateLost() {
        return dateLost;
    }
    public String getDateFound() {
        return dateFound;
    }
    public String getTag() {
        return tag;
    }
    public String getItemName() {
        return itemName;
    }
}