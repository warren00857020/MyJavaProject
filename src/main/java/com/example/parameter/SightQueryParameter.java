package com.example.parameter;

public class SightQueryParameter {
    private String city;      // 城市名稱，例如：台北市
    private String zone;      // 區域名稱，例如：中正區
    private String keyword;   // 關鍵字搜尋（景點名稱、描述等）
    private String orderBy;
    private String sortRule;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getSortRule() {
        return sortRule;
    }

    public void setSortRule(String sortRule) {
        this.sortRule = sortRule;
    }
}