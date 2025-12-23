package com.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "regions")
public class Region {

    /* ---------- 主鍵 ---------- */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ---------- 基本欄位 ---------- */
    @Column(nullable = false, unique = true, length = 50)
    private String name;  // 縣市名稱，如：台北市、台南市

    @Column(nullable = false, unique = true, length = 10)
    private String code;  // 縣市代碼，如：TPE, TNN

    @Column(name = "region_type", nullable = false, length = 20)
    private String regionType;  // 類型：city, county

    @Column(columnDefinition = "TEXT") //直接指定欄位類型為TEXT(PostgreSQL)
    private String description;  // 區域描述

    /* ---------- 時間戳記 ---------- */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /* ---------- 無參構造函式 ---------- */
    public Region() {}

    /* ---------- 有參構造函式 ---------- */
    public Region(String name, String code, String regionType) {
        this.name = name;
        this.code = code;
        this.regionType = regionType;
    }

    /* ---------- Getter / Setter ---------- */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getRegionType() { return regionType; }
    public void setRegionType(String regionType) { this.regionType = regionType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
