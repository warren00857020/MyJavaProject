package com.example.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sights", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sight_name", "region_id"})
})
public class Sight {

    /* ---------- 主鍵 ---------- */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ---------- 基本欄位 ---------- */
    @Column(name = "sight_name", nullable = false, length = 255)
    private String sightName;

    @Column(name = "region_id")
    private Long regionId;  // 關聯縣市 (之後可改為 @ManyToOne)

    @Column(length = 100)
    private String zone;

    @Column(length = 100)
    private String category;

    /* ---------- 照片 ---------- */
    @Column(name = "photo_url", length = 1024)
    private String photoURL;

    @Column(name = "photo_urls", columnDefinition = "TEXT")
    private String photoUrls;  // 逗號分隔的照片 URL 字串

    /* ---------- 描述與地址 ---------- */
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String address;

    /* ---------- 地理位置 ---------- */
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    /* ---------- 營業與票價資訊 ---------- */
    @Column(name = "opening_hours", columnDefinition = "TEXT")
    private String openingHours;  // JSON 字串格式

    @Column(name = "ticket_price", columnDefinition = "TEXT")
    private String ticketPrice;

    @Column(name = "official_website", length = 512)
    private String officialWebsite;

    @Column(columnDefinition = "TEXT")
    private String phone;

    /* ---------- 標籤與推薦 ---------- */
    @Column(columnDefinition = "TEXT")
    private String tags;  // 逗號分隔的標籤字串

    @Column(name = "recommended_duration")
    private Integer recommendedDuration;  // 建議停留時間（分鐘）

    /* ---------- 元資料 ---------- */
    @Column(name = "source_url", length = 512)
    private String sourceUrl;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "view_count")
    private Integer viewCount = 0;

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

    /* ---------- 無參構造函式（JPA 規定必須要有） ---------- */
    public Sight() {}

    /* ---------- Getter / Setter ---------- */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSightName() { return sightName; }
    public void setSightName(String sightName) { this.sightName = sightName; }

    public Long getRegionId() { return regionId; }
    public void setRegionId(Long regionId) { this.regionId = regionId; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPhotoURL() { return photoURL; }
    public void setPhotoURL(String photoURL) { this.photoURL = photoURL; }

    public String getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(String photoUrls) { this.photoUrls = photoUrls; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }

    public String getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(String ticketPrice) { this.ticketPrice = ticketPrice; }

    public String getOfficialWebsite() { return officialWebsite; }
    public void setOfficialWebsite(String officialWebsite) { this.officialWebsite = officialWebsite; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Integer getRecommendedDuration() { return recommendedDuration; }
    public void setRecommendedDuration(Integer recommendedDuration) { this.recommendedDuration = recommendedDuration; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
