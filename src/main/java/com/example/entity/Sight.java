package com.example.entity;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sight")
public class Sight {
    private String sightName = "";
    private String zone = "";
    private String category = "";
    private String photoURL = "";
    private String description = "";
    private String address = "";
    public Sight(){

    }
    public Sight (String SightName,String Zone,String Category,String PhotoURL,String Description,String Address){
        this.sightName = SightName;
        this.zone = Zone;
        this.category = Category;
        this.photoURL = PhotoURL;
        this.description = Description;
        this.address = Address;
    }
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
