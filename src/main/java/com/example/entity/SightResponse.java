package com.example.entity;

public class SightResponse {
    private String sightName = "";
    private String zone = "";
    private String category = "";
    private String photoURL = "";
    private String description = "";
    private String address = "";

    public String getAddress() {return address;}

    public String getCategory() {return category; }

    public String getDescription() {return description; }

    public String getSightName() {
        return sightName;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public String getZone() {
        return zone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSightName(String sightName) {
        this.sightName = sightName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }
}
