package com.example.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "festivals")
public class Festival {

    /* ---------- 主鍵 ---------- */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ---------- 基本欄位 ---------- */
    @Column(name = "festival_name", nullable = false, length = 255)
    private String festivalName;  // 節慶名稱

    @Column(name = "region_id")
    private Long regionId;  // 關聯縣市

    @Column(length = 100)
    private String zone;  // 舉辦區域

    @Column(length = 100)
    private String category;  // 分類：傳統節慶、音樂祭、藝術展等

    /* ---------- 照片 ---------- */
    @Column(name = "photo_url", length = 1024)
    private String photoUrl;

    @Column(name = "photo_urls", columnDefinition = "TEXT")
    private String photoUrls;  // 逗號分隔的照片 URL 字串

    /* ---------- 描述與地點 ---------- */
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String location;  // 舉辦地點

    @Column(length = 255)
    private String address;

    /* ---------- 地理位置 ---------- */
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    /* ---------- 時間資訊 ---------- */
    @Column(name = "start_date")
    private LocalDate startDate;  // 開始日期

    @Column(name = "end_date")
    private LocalDate endDate;  // 結束日期

    @Column(name = "recurring_pattern", length = 100)
    private String recurringPattern;  // 週期性：annual, monthly, seasonal

    @Column(name = "month_held")
    private Integer monthHeld;  // 舉辦月份（農曆或國曆）

    @Column(name = "is_lunar")
    private Boolean isLunar = false;  // 是否為農曆

    @Column(name = "event_schedule", columnDefinition = "TEXT")
    private String eventSchedule;  // 活動時程（JSON 格式）

    /* ---------- 聯絡資訊 ---------- */
    @Column(name = "official_website", length = 512)
    private String officialWebsite;

    @Column(name = "contact_info", length = 255)
    private String contactInfo;

    /* ---------- 標籤與參與資訊 ---------- */
    @Column(columnDefinition = "TEXT")
    private String tags;  // 逗號分隔的標籤字串（UNESCO、國定、地方特色等）

    @Column(name = "expected_duration")
    private Integer expectedDuration;  // 預計參與時間（分鐘）

    @Column(name = "ticket_required")
    private Boolean ticketRequired = false;  // 是否需要門票

    @Column(name = "ticket_info", columnDefinition = "TEXT")
    private String ticketInfo;  // 票務資訊

    /* ---------- 元資料 ---------- */
    @Column(name = "source_url", length = 512, unique = true)
    private String sourceUrl;  // taiwan.net.tw 的原始 URL（用於去重）

    @Column(name = "event_id", length = 50)
    private String eventId;  // 活動編號（例：lid=081434）

    @Column(name = "date_string", length = 100)
    private String dateString;  // 原始日期字串（例：2025-11-14~2025-12-28）

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
    public Festival() {}

    /* ---------- Getter / Setter ---------- */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFestivalName() { return festivalName; }
    public void setFestivalName(String festivalName) { this.festivalName = festivalName; }

    public Long getRegionId() { return regionId; }
    public void setRegionId(Long regionId) { this.regionId = regionId; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(String photoUrls) { this.photoUrls = photoUrls; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getRecurringPattern() { return recurringPattern; }
    public void setRecurringPattern(String recurringPattern) { this.recurringPattern = recurringPattern; }

    public Integer getMonthHeld() { return monthHeld; }
    public void setMonthHeld(Integer monthHeld) { this.monthHeld = monthHeld; }

    public Boolean getIsLunar() { return isLunar; }
    public void setIsLunar(Boolean isLunar) { this.isLunar = isLunar; }

    public String getEventSchedule() { return eventSchedule; }
    public void setEventSchedule(String eventSchedule) { this.eventSchedule = eventSchedule; }

    public String getOfficialWebsite() { return officialWebsite; }
    public void setOfficialWebsite(String officialWebsite) { this.officialWebsite = officialWebsite; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Integer getExpectedDuration() { return expectedDuration; }
    public void setExpectedDuration(Integer expectedDuration) { this.expectedDuration = expectedDuration; }

    public Boolean getTicketRequired() { return ticketRequired; }
    public void setTicketRequired(Boolean ticketRequired) { this.ticketRequired = ticketRequired; }

    public String getTicketInfo() { return ticketInfo; }
    public void setTicketInfo(String ticketInfo) { this.ticketInfo = ticketInfo; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getDateString() { return dateString; }
    public void setDateString(String dateString) { this.dateString = dateString; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
