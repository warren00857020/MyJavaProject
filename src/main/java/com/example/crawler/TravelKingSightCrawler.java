package com.example.crawler;

import com.example.crawler.config.CrawlerConfig;
import com.example.entity.Sight;
import com.example.repository.SightRepository;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TravelKing 景點爬蟲
 * 爬取 https://www.travelking.com.tw 的景點資訊
 */
@Component
public class TravelKingSightCrawler extends BaseTravelKingCrawler<Sight> {

    @Autowired
    private SightRepository sightRepository;

    @Override
    protected String getListUrl(String city) {
        // 例：https://www.travelking.com.tw/tourguide/taiwan/hualien-county
        return CrawlerConfig.BASE_URL + "/tourguide/taiwan/" + city;
    }

    @Override
    protected Sight parseDetailPage(Document doc, String url) {
        try {
            Sight sight = new Sight();

            // 1. 景點名稱 (從 <span property="dc:title">)
            Element titleSpan = doc.selectFirst("span[property='dc:title']");
            if (titleSpan != null) {
                sight.setSightName(titleSpan.text().trim());
            } else {
                // 備用方案：從 <h1> 抓取
                Element h1 = doc.selectFirst("h1");
                if (h1 != null) {
                    sight.setSightName(h1.text().trim());
                } else {
                    logger.warn("未找到景點名稱：{}", url);
                    return null;
                }
            }

            // 2. 地址 (從 <span property="vcard:street-address">)
            Element addressSpan = doc.selectFirst("span[property='vcard:street-address']");
            if (addressSpan != null) {
                String fullAddress = addressSpan.text().trim();
                sight.setAddress(fullAddress);

                // 從地址中提取行政區 (例：「新北市八里區...」-> 「八里區」)
                String zone = extractZoneFromAddress(fullAddress);
                sight.setZone(zone);
            }

            // 3. 景點類別 (從 <span property="rdfs:label"><strong>)
            Element categoryLabel = doc.selectFirst("span[property='rdfs:label'] strong");
            if (categoryLabel != null) {
                sight.setCategory(categoryLabel.text().trim());
            }

            // 4. 景點描述 (從 <div class="text" property="dc:description">)
            Element descDiv = doc.selectFirst("div.text[property='dc:description']");
            if (descDiv != null) {
                // 移除標題 <h4>，只保留內容
                Elements h4s = descDiv.select("h4");
                h4s.remove();

                String description = descDiv.text().trim();
                // 限制長度，避免太長
                if (description.length() > 1000) {
                    description = description.substring(0, 1000) + "...";
                }
                sight.setDescription(description);
            }

            // 5. 照片 URLs (從圖片輪播或主要圖片)
            String photoUrls = extractPhotoUrls(doc);
            sight.setPhotoUrls(photoUrls);

            // 設定第一張照片為主照片
            if (photoUrls != null && !photoUrls.isEmpty()) {
                String firstPhoto = photoUrls.split(",")[0];
                sight.setPhotoURL(firstPhoto);
            }

            // 6. 建議停留時間 (從 <div class="point_list"><h3>建議停留時間)
            String duration = extractPointListValue(doc, "建議停留時間");
            if (duration != null) {
                sight.setRecommendedDuration(parseDuration(duration));
            }

            // 7. 票價資訊
            String ticketPrice = extractPointListValue(doc, "費用簡介");
            sight.setTicketPrice(ticketPrice);

            // 8. 電話
            String phone = extractByLabel(doc, "電話");
            sight.setPhone(phone);

            // 9. 營業時間
            String openingHours = extractOpeningHours(doc);
            sight.setOpeningHours(openingHours);

            // 10. 經緯度 (從 Google Maps 連結解析)
            parseLatLng(doc, sight);

            // 11. 人氣指數 (從 <span class="hotvalue">)
            Element hotValue = doc.selectFirst("span.hotvalue b");
            if (hotValue != null) {
                try {
                    Integer viewCount = Integer.parseInt(hotValue.text().trim());
                    sight.setViewCount(viewCount);
                } catch (NumberFormatException e) {
                    sight.setViewCount(0);
                }
            }

            // 12. 來源 URL
            sight.setSourceUrl(url);

            logger.info("✅ 成功解析景點：{} ({})", sight.getSightName(), sight.getZone());
            return sight;

        } catch (Exception e) {
            logger.error("❌ 解析景點詳細頁失敗：{}", url, e);
            return null;
        }
    }

    @Override
    protected void saveItem(Sight item) {
        try {
            // 根據 sourceUrl 去重
            Optional<Sight> existing = sightRepository.findBySourceUrl(item.getSourceUrl());

            if (existing.isPresent()) {
                // 更新現有資料（保留 ID 和建立時間）
                Sight existingSight = existing.get();
                item.setId(existingSight.getId());
                item.setCreatedAt(existingSight.getCreatedAt());
                logger.info("更新景點: {} (URL: {})", item.getSightName(), item.getSourceUrl());
            } else {
                logger.info("新增景點: {}", item.getSightName());
            }

            sightRepository.save(item);
        } catch (Exception e) {
            logger.error("儲存景點失敗：{}", item.getSightName(), e);
        }
    }

    @Override
    protected void saveBatch(List<Sight> items) {
        try {
            sightRepository.saveAll(items);
            logger.info("批次儲存 {} 筆景點資料", items.size());
        } catch (Exception e) {
            logger.error("批次儲存失敗", e);
        }
    }

    /**
     * 公開方法：爬取單一景點 URL
     * 用於手動提供 URL 列表時使用
     *
     * @param url 景點詳細頁 URL
     * @return 景點物件，失敗返回 null
     */
    public Sight crawlSingleUrl(String url) {
        logger.info("開始爬取單一景點：{}", url);

        try {
            // 獲取詳細頁
            org.jsoup.nodes.Document doc = fetchDocument(url);

            if (doc == null) {
                logger.error("無法獲取景點詳細頁：{}", url);
                return null;
            }

            // 解析景點資料
            Sight sight = parseDetailPage(doc, url);

            // 儲存到資料庫
            if (sight != null) {
                saveItem(sight);
                logger.info("✅ 成功爬取並儲存景點：{}", sight.getSightName());
            }

            return sight;

        } catch (Exception e) {
            logger.error("❌ 爬取單一景點失敗：{}", url, e);
            return null;
        }
    }

    /**
     * 根據標籤提取資訊
     * 例：「地址：台北市...」-> 提取地址
     */
    private String extractByLabel(Document doc, String label) {
        // 尋找包含標籤的元素
        Elements elements = doc.getElementsContainingOwnText(label);

        for (Element element : elements) {
            String text = element.text();
            // 移除標籤部分
            String value = text.replaceFirst(label + "[:：]?\\s*", "").trim();
            if (!value.isEmpty() && !value.equals(text)) {
                return value;
            }
        }

        return null;
    }


    /**
     * 提取照片 URLs（返回逗號分隔的字串）
     */
    private String extractPhotoUrls(Document doc) {
        List<String> urls = new ArrayList<>();

        // 1. 嘗試從輪播圖提取 (Swiper)
        Elements swiperImages = doc.select(".swiper-slide img, .slider img, .carousel img");
        for (Element img : swiperImages) {
            String src = img.attr("abs:src");  // 獲取絕對路徑
            if (!src.isEmpty()) {
                urls.add(src);
            }
        }

        // 2. 如果沒有輪播，取主要圖片
        if (urls.isEmpty()) {
            Elements mainImages = doc.select("img[src*=scenery], img[src*=image], .photo img");
            for (Element img : mainImages) {
                String src = img.attr("abs:src");
                if (!src.isEmpty()) {
                    urls.add(src);
                }
            }
        }

        // 返回逗號分隔的字串
        return String.join(",", urls);
    }

    /**
     * 提取營業時間
     */
    private String extractOpeningHours(Document doc) {
        String hours = extractByLabel(doc, "營業時間");
        if (hours == null) {
            hours = extractByLabel(doc, "開放時間");
        }
        return hours;
    }

    /**
     * 從地址中提取行政區
     * 例：「新北市八里區博物館路...」-> 「八里區」
     */
    private String extractZoneFromAddress(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }

        // 正則：匹配「XX市/縣 + XX區/鄉/鎮/市」
        Pattern pattern = Pattern.compile("([^市縣]+[市縣])([^區鄉鎮市]+[區鄉鎮市])");
        Matcher matcher = pattern.matcher(address);

        if (matcher.find()) {
            // 返回行政區部分（例：「八里區」）
            return matcher.group(2);
        }

        // 如果沒有符合，嘗試直接找「XX區/鄉/鎮/市」
        Pattern zonePattern = Pattern.compile("([^區鄉鎮市]+[區鄉鎮市])");
        Matcher zoneMatcher = zonePattern.matcher(address);
        if (zoneMatcher.find()) {
            return zoneMatcher.group(1);
        }

        return null;
    }

    /**
     * 從 <div class="point_list"> 中提取值
     * 例：<div class="point_list"><h3>建議停留時間：</h3><p>90分鐘</p></div>
     */
    private String extractPointListValue(Document doc, String label) {
        Elements pointLists = doc.select("div.point_list");

        for (Element div : pointLists) {
            Element h3 = div.selectFirst("h3");
            if (h3 != null && h3.text().contains(label)) {
                Element p = div.selectFirst("p");
                if (p != null) {
                    return p.text().trim();
                }
            }
        }

        return null;
    }

    /**
     * 解析停留時間字串，轉換為分鐘數
     * 例：「90分鐘」-> 90, 「1小時」-> 60, 「1.5小時」-> 90
     */
    private Integer parseDuration(String durationText) {
        if (durationText == null || durationText.isEmpty()) {
            return null;
        }

        try {
            // 處理「90分鐘」格式
            Pattern minutePattern = Pattern.compile("(\\d+)\\s*分鐘");
            Matcher minuteMatcher = minutePattern.matcher(durationText);
            if (minuteMatcher.find()) {
                return Integer.parseInt(minuteMatcher.group(1));
            }

            // 處理「1小時」或「1.5小時」格式
            Pattern hourPattern = Pattern.compile("([\\d.]+)\\s*小時");
            Matcher hourMatcher = hourPattern.matcher(durationText);
            if (hourMatcher.find()) {
                double hours = Double.parseDouble(hourMatcher.group(1));
                return (int) (hours * 60);
            }
        } catch (Exception e) {
            logger.debug("停留時間解析失敗：{}", durationText);
        }

        return null;
    }

    /**
     * 從 Google Maps 連結解析經緯度
     */
    private void parseLatLng(Document doc, Sight sight) {
        // 尋找 Google Maps 連結
        Element mapLink = doc.selectFirst("a[href*=google.com/maps], a[href*=maps.google]");

        if (mapLink != null) {
            String href = mapLink.attr("href");

            // 格式1：/maps/search/25.16391550,121.41261522
            Pattern pattern1 = Pattern.compile("/maps/search/([\\d.]+),([\\d.]+)");
            Matcher matcher1 = pattern1.matcher(href);
            if (matcher1.find()) {
                sight.setLatitude(new BigDecimal(matcher1.group(1)));
                sight.setLongitude(new BigDecimal(matcher1.group(2)));
                logger.debug("解析到經緯度：{}, {}", matcher1.group(1), matcher1.group(2));
                return;
            }

            // 格式2：q=24.0770862685263,120.549057732064
            Pattern pattern2 = Pattern.compile("q=([\\d.]+),([\\d.]+)");
            Matcher matcher2 = pattern2.matcher(href);
            if (matcher2.find()) {
                sight.setLatitude(new BigDecimal(matcher2.group(1)));
                sight.setLongitude(new BigDecimal(matcher2.group(2)));
                return;
            }

            // 格式3：@24.0770862685263,120.549057732064
            Pattern pattern3 = Pattern.compile("@([\\d.]+),([\\d.]+)");
            Matcher matcher3 = pattern3.matcher(href);
            if (matcher3.find()) {
                sight.setLatitude(new BigDecimal(matcher3.group(1)));
                sight.setLongitude(new BigDecimal(matcher3.group(2)));
                return;
            }
        }
    }

    /**
     * 提取人氣指數
     */
    private Integer extractViewCount(Document doc) {
        String viewText = extractByLabel(doc, "人氣指數");
        if (viewText == null) {
            viewText = extractByLabel(doc, "瀏覽人數");
        }

        if (viewText != null) {
            try {
                // 移除非數字字符
                String numberOnly = viewText.replaceAll("[^0-9]", "");
                return Integer.parseInt(numberOnly);
            } catch (NumberFormatException e) {
                logger.debug("人氣指數解析失敗：{}", viewText);
            }
        }

        return null;
    }
}
