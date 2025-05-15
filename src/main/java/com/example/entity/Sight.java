package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sights")          // ⇦ PostgreSQL 表名，可自行調整
public class Sight {

    /* ---------- 主鍵 ---------- */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // BIGSERIAL 自增
    private Long id;

    /* ---------- 欄位 ---------- */
    @Column(name = "sight_name", nullable = false, unique = true, length = 255)
    private String sightName = "";

    @Column(nullable = false, length = 100)
    private String zone = "";

    @Column(length = 100)
    private String category = "";

    @Column(length = 1024)
    private String photoURL = "";

    @Column(columnDefinition = "TEXT")
    private String description = "";

    @Column(length = 255)
    private String address = "";

    /* ---------- 無參構造函式（JPA 規定必須要有） ---------- */
    public Sight() {}

    /* ---------- 多參構造函式（方便新建物件） ---------- */
    public Sight(String sightName,
                 String zone,
                 String category,
                 String photoURL,
                 String description,
                 String address) {
        this.sightName   = sightName;
        this.zone        = zone;
        this.category    = category;
        this.photoURL    = photoURL;
        this.description = description;
        this.address     = address;
    }

    /* ---------- Getter / Setter ---------- */
    public Long   getId()          { return id; }
    public String getSightName()   { return sightName; }
    public String getZone()        { return zone; }
    public String getCategory()    { return category; }
    public String getPhotoURL()    { return photoURL; }
    public String getDescription() { return description; }
    public String getAddress()     { return address; }

    public void setId(Long id)                     { this.id = id; }
    public void setSightName(String sightName)     { this.sightName = sightName; }
    public void setZone(String zone)               { this.zone = zone; }
    public void setCategory(String category)       { this.category = category; }
    public void setPhotoURL(String photoURL)       { this.photoURL = photoURL; }
    public void setDescription(String description) { this.description = description; }
    public void setAddress(String address)         { this.address = address; }

    /* ---------- toString()（除錯用） ---------- */
    @Override
    public String toString() {
        return "SightName : " + sightName +
                "\nZone      : " + zone +
                "\nCategory  : " + category +
                "\nPhotoURL  : " + photoURL +
                "\nDescription: " + description +
                "\nAddress   : " + address + "\n";
    }
}
