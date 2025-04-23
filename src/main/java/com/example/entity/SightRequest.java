package com.example.entity;


import jakarta.validation.constraints.NotEmpty;

//分別建立請求主體與資料庫文件的類別
public class SightRequest {
    @NotEmpty(message = "Sight's name is undefined")
    private String sightName = "";
    @NotEmpty(message = "Sight's zone is undefined")
    private String zone = "";
    @NotEmpty(message = "Sight's category is undefined")
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

    @Override
    public String toString(){
        return "SightName : "+getSightName()+"\nZone : "+getZone()+"\nCategory : "+getCategory()+"\nPhotoURL : "+getPhotoURL()+"\nDescription : "+getDescription()+"\nAddress : "+getAddress()+"\n";

    }
}
