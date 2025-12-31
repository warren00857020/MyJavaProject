package com.example.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "foods", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"food_name", "address"})
})
public class Food {

    /* ---------- 主鍵 ---------- */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ---------- Google Places API 欄位 ---------- */
    @Column(name = "place_id", unique = true, length = 255)
    private String placeId;  // Google Place ID（唯一識別碼，用於更新與去重）

    @Column(name = "rating")
    private Double rating;  // Google 評分 (0.0-5.0)

    @Column(name = "user_ratings_total")
    private Integer userRatingsTotal;  // Google 評價數量

    @Column(name = "price_level")
    private Integer priceLevel;  // Google 價格等級 (0-4: 0=免費, 1=$, 2=$$, 3=$$$, 4=$$$$)

    @Column(name = "business_status", length = 50)
    private String businessStatus;  // 營業狀態：OPERATIONAL, CLOSED_TEMPORARILY, CLOSED_PERMANENTLY

    @Column(name = "google_types", columnDefinition = "TEXT[]")
    private String[] googleTypes;  // Google 類型陣列 ["restaurant", "food", "point_of_interest"]

    @Column(name = "photo_references", columnDefinition = "TEXT[]")
    private String[] photoReferences;  // Google Photos 參考 ID 陣列（用於後續取得實際圖片）

    /* ---------- 基本欄位 ---------- */
    @Column(name = "food_name", nullable = false, length = 255)
    private String foodName;  // 美食/餐廳名稱

    @Column(name = "region_id")
    private Long regionId;  // 關聯縣市

    @Column(length = 100)
    private String zone;  // 行政區

    @Column(length = 100)
    private String category;  // 分類：小吃、餐廳、甜點、夜市等

    @Column(name = "cuisine_type", length = 100)
    private String cuisineType;  // 菜系：台菜、客家菜、原住民料理等

    /* ---------- 照片 ---------- */
    @Column(name = "photo_url", length = 1024)
    private String photoUrl;

    @Column(name = "photo_urls", columnDefinition = "TEXT[]")
    private String[] photoUrls;

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

    /* ---------- 營業與價格資訊 ---------- */
    @Column(name = "opening_hours", columnDefinition = "TEXT")
    private String openingHours;  // JSON 字串格式（儲存為 TEXT）

    @Column(name = "price_range", length = 50)
    private String priceRange;  // 價格範圍：$, $$, $$$

    @Column(name = "signature_dishes", columnDefinition = "TEXT[]")
    private String[] signatureDishes;  // 招牌菜色陣列

    @Column(length = 50)
    private String phone;

    @Column(name = "official_website", length = 512)
    private String officialWebsite;

    /* ---------- 標籤與推薦 ---------- */
    @Column(columnDefinition = "TEXT[]")
    private String[] tags;  // 標籤：米其林、必吃、在地推薦等

    @Column(name = "avg_meal_duration")
    private Integer avgMealDuration;  // 平均用餐時間（分鐘）

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

    /* ---------- 無參構造函式 ---------- */
    public Food() {}

    /* ---------- Getter / Setter ---------- */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // Google Places API 欄位
    public String getPlaceId() { return placeId; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getUserRatingsTotal() { return userRatingsTotal; }
    public void setUserRatingsTotal(Integer userRatingsTotal) { this.userRatingsTotal = userRatingsTotal; }

    public Integer getPriceLevel() { return priceLevel; }
    public void setPriceLevel(Integer priceLevel) { this.priceLevel = priceLevel; }

    public String getBusinessStatus() { return businessStatus; }
    public void setBusinessStatus(String businessStatus) { this.businessStatus = businessStatus; }

    public String[] getGoogleTypes() { return googleTypes; }
    public void setGoogleTypes(String[] googleTypes) { this.googleTypes = googleTypes; }

    public String[] getPhotoReferences() { return photoReferences; }
    public void setPhotoReferences(String[] photoReferences) { this.photoReferences = photoReferences; }

    // 基本欄位
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public Long getRegionId() { return regionId; }
    public void setRegionId(Long regionId) { this.regionId = regionId; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCuisineType() { return cuisineType; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String[] getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(String[] photoUrls) { this.photoUrls = photoUrls; }

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

    public String getPriceRange() { return priceRange; }
    public void setPriceRange(String priceRange) { this.priceRange = priceRange; }

    public String[] getSignatureDishes() { return signatureDishes; }
    public void setSignatureDishes(String[] signatureDishes) { this.signatureDishes = signatureDishes; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getOfficialWebsite() { return officialWebsite; }
    public void setOfficialWebsite(String officialWebsite) { this.officialWebsite = officialWebsite; }

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }

    public Integer getAvgMealDuration() { return avgMealDuration; }
    public void setAvgMealDuration(Integer avgMealDuration) { this.avgMealDuration = avgMealDuration; }

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
