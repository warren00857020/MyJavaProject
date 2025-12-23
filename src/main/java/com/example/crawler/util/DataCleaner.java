package com.example.crawler.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 資料清洗工具類
 * 用於清理和標準化爬取的資料
 */
@Component
public class DataCleaner {

    /**
     * 清理地址
     * - 移除多餘空白
     * - 統一格式
     */
    public String cleanAddress(String rawAddress) {
        if (rawAddress == null || rawAddress.isEmpty()) {
            return null;
        }

        String cleaned = rawAddress
            .replaceAll("\\s+", " ")  // 多個空白替換為單一空白
            .replaceAll("　", " ")     // 全形空白替換為半形
            .trim();

        // 移除常見的多餘文字
        cleaned = cleaned.replaceAll("地址[:：]?\\s*", "");

        return cleaned.isEmpty() ? null : cleaned;
    }

    /**
     * 清理電話號碼
     * - 統一格式：(02)2345-6789 或 0912-345-678
     */
    public String cleanPhone(String rawPhone) {
        if (rawPhone == null || rawPhone.isEmpty()) {
            return null;
        }

        // 移除所有非數字、非括號、非連字號的字符
        String cleaned = rawPhone.replaceAll("[^0-9()\\-]", "");

        // 標準化格式
        // 市話格式：02-23456789 -> (02)2345-6789
        Pattern landlinePattern = Pattern.compile("^(\\d{2,3})[\\-]?(\\d{4})[\\-]?(\\d{4})$");
        Matcher landlineMatcher = landlinePattern.matcher(cleaned);
        if (landlineMatcher.matches()) {
            return "(" + landlineMatcher.group(1) + ")" +
                   landlineMatcher.group(2) + "-" + landlineMatcher.group(3);
        }

        // 手機格式：0912345678 -> 0912-345-678
        Pattern mobilePattern = Pattern.compile("^(09\\d{2})(\\d{3})(\\d{3})$");
        Matcher mobileMatcher = mobilePattern.matcher(cleaned);
        if (mobileMatcher.matches()) {
            return mobileMatcher.group(1) + "-" +
                   mobileMatcher.group(2) + "-" + mobileMatcher.group(3);
        }

        return cleaned.isEmpty() ? null : cleaned;
    }

    /**
     * 從描述中提取標籤/關鍵字
     * 使用關鍵字比對的方式
     */
    public String[] extractTags(String description) {
        if (description == null || description.isEmpty()) {
            return new String[0];
        }

        List<String> tags = new ArrayList<>();

        // 定義關鍵字列表
        String[] keywords = {
            "古蹟", "歷史", "文化", "自然", "風景", "海灘", "山", "湖",
            "廟宇", "寺廟", "教堂", "博物館", "美術館", "公園", "遊樂園",
            "夜市", "老街", "購物", "美食", "溫泉", "登山", "健行",
            "賞花", "賞櫻", "賞楓", "觀光", "親子", "寵物友善", "無障礙"
        };

        // 檢查描述中是否包含關鍵字
        for (String keyword : keywords) {
            if (description.contains(keyword) && !tags.contains(keyword)) {
                tags.add(keyword);
            }
        }

        return tags.toArray(new String[0]);
    }

    /**
     * 清理營業時間文字
     */
    public String cleanOpeningHours(String rawHours) {
        if (rawHours == null || rawHours.isEmpty()) {
            return null;
        }

        String cleaned = rawHours
            .replaceAll("\\s+", " ")
            .replaceAll("營業時間[:：]?\\s*", "")
            .replaceAll("開放時間[:：]?\\s*", "")
            .trim();

        return cleaned.isEmpty() ? null : cleaned;
    }

    /**
     * 清理分類名稱
     */
    public String cleanCategory(String rawCategory) {
        if (rawCategory == null || rawCategory.isEmpty()) {
            return null;
        }

        String cleaned = rawCategory
            .replaceAll("\\s+", " ")
            .replaceAll("景點類別[:：]?\\s*", "")
            .replaceAll("類別[:：]?\\s*", "")
            .trim();

        return cleaned.isEmpty() ? null : cleaned;
    }

    /**
     * 清理描述文字
     * - 移除多餘空白和換行
     * - 限制長度
     */
    public String cleanDescription(String rawDescription, int maxLength) {
        if (rawDescription == null || rawDescription.isEmpty()) {
            return null;
        }

        String cleaned = rawDescription
            .replaceAll("\\s+", " ")  // 多個空白替換為單一空白
            .replaceAll("　", " ")     // 全形空白替換為半形
            .trim();

        // 限制長度
        if (cleaned.length() > maxLength) {
            cleaned = cleaned.substring(0, maxLength) + "...";
        }

        return cleaned.isEmpty() ? null : cleaned;
    }

    /**
     * 驗證並清理 URL
     */
    public String cleanUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isEmpty()) {
            return null;
        }

        String cleaned = rawUrl.trim();

        // 確保使用 https
        if (cleaned.startsWith("http://")) {
            cleaned = cleaned.replace("http://", "https://");
        }

        return cleaned;
    }

    /**
     * 清理圖片 URL 陣列
     */
    public String[] cleanPhotoUrls(String[] rawUrls) {
        if (rawUrls == null || rawUrls.length == 0) {
            return new String[0];
        }

        List<String> cleanedUrls = new ArrayList<>();

        for (String url : rawUrls) {
            String cleaned = cleanUrl(url);
            if (cleaned != null && !cleanedUrls.contains(cleaned)) {
                cleanedUrls.add(cleaned);
            }
        }

        return cleanedUrls.toArray(new String[0]);
    }

    /**
     * 移除 HTML 標籤
     */
    public String removeHtmlTags(String html) {
        if (html == null || html.isEmpty()) {
            return null;
        }

        return html.replaceAll("<[^>]+>", "").trim();
    }
}
