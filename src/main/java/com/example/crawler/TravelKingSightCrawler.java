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
    protected String getListUrl(String county, String city) {
        // 例：https://www.travelking.com.tw/tourguide/taipei/taipei-city/
        return CrawlerConfig.BASE_URL + "/tourguide/" + county + "/" + city + "/";
    }

    @Override
    protected Sight parseDetailPage(Document doc, String url) {
        try {
            Sight sight = new Sight();

            // 1. 景點名稱 (從 <h1> 標籤)
            Element h1 = doc.selectFirst("h1");
            if (h1 != null) {
                sight.setSightName(h1.text().trim());
            } else {
                logger.warn("未找到景點名稱：{}", url);
                return null;
            }

            // 2. 地址 (從頁面中尋找包含「地址」的文字)
            String address = extractByLabel(doc, "地址");
            sight.setAddress(address);

            // 3. 景點類別
            String category = extractByLabel(doc, "景點類別");
            sight.setCategory(category);

            // 4. 景點描述 (從簡介段落)
            String description = extractDescription(doc);
            sight.setDescription(description);

            // 5. 照片 URLs (從圖片輪播或主要圖片)
            String[] photoUrls = extractPhotoUrls(doc);
            sight.setPhotoUrls(photoUrls);

            // 6. 營業時間
            String openingHours = extractOpeningHours(doc);
            sight.setOpeningHours(openingHours);

            // 7. 電話
            String phone = extractByLabel(doc, "電話");
            sight.setPhone(phone);

            // 8. 經緯度 (從 Google Maps 連結解析)
            parseLatLng(doc, sight);

            // 9. 人氣指數
            Integer viewCount = extractViewCount(doc);
            sight.setViewCount(viewCount != null ? viewCount : 0);

            // 10. 設定 region_id (需要從 URL 或其他地方推斷，暫時設為 null)
            // TODO: 根據 county 映射到 region_id

            logger.debug("成功解析景點：{}", sight.getSightName());
            return sight;

        } catch (Exception e) {
            logger.error("解析景點詳細頁失敗：{}", url, e);
            return null;
        }
    }

    @Override
    protected void saveItem(Sight item) {
        try {
            sightRepository.save(item);
            logger.debug("儲存景點：{}", item.getSightName());
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
     * 提取景點描述
     */
    private String extractDescription(Document doc) {
        StringBuilder description = new StringBuilder();

        // 嘗試從簡介區塊提取
        Element intro = doc.selectFirst("article, .content, .description, .intro");
        if (intro != null) {
            // 獲取所有段落
            Elements paragraphs = intro.select("p");
            for (Element p : paragraphs) {
                String text = p.text().trim();
                if (!text.isEmpty()) {
                    description.append(text).append("\n");
                }
            }
        }

        // 如果沒有找到，嘗試其他方式
        if (description.length() == 0) {
            Element body = doc.body();
            Elements allParagraphs = body.select("p");
            for (Element p : allParagraphs) {
                String text = p.text().trim();
                if (text.length() > 50) {  // 只取較長的段落
                    description.append(text).append("\n");
                    if (description.length() > 500) break;  // 限制長度
                }
            }
        }

        return description.toString().trim();
    }

    /**
     * 提取照片 URLs
     */
    private String[] extractPhotoUrls(Document doc) {
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

        return urls.toArray(new String[0]);
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
     * 從 Google Maps 連結解析經緯度
     */
    private void parseLatLng(Document doc, Sight sight) {
        // 尋找 Google Maps 連結
        Element mapLink = doc.selectFirst("a[href*=google.com/maps], a[href*=maps.google]");

        if (mapLink != null) {
            String href = mapLink.attr("href");

            // 嘗試多種格式
            // 格式1：q=24.0770862685263,120.549057732064
            Pattern pattern1 = Pattern.compile("q=(\\d+\\.\\d+),(\\d+\\.\\d+)");
            Matcher matcher1 = pattern1.matcher(href);
            if (matcher1.find()) {
                sight.setLatitude(new BigDecimal(matcher1.group(1)));
                sight.setLongitude(new BigDecimal(matcher1.group(2)));
                return;
            }

            // 格式2：@24.0770862685263,120.549057732064
            Pattern pattern2 = Pattern.compile("@(\\d+\\.\\d+),(\\d+\\.\\d+)");
            Matcher matcher2 = pattern2.matcher(href);
            if (matcher2.find()) {
                sight.setLatitude(new BigDecimal(matcher2.group(1)));
                sight.setLongitude(new BigDecimal(matcher2.group(2)));
                return;
            }
        }

        // 如果沒有 Google Maps 連結，嘗試從頁面中的其他地方找經緯度
        String pageText = doc.text();
        Pattern coordPattern = Pattern.compile("(\\d{2}\\.\\d+)[,，]\\s*(\\d{2,3}\\.\\d+)");
        Matcher coordMatcher = coordPattern.matcher(pageText);
        if (coordMatcher.find()) {
            try {
                BigDecimal lat = new BigDecimal(coordMatcher.group(1));
                BigDecimal lng = new BigDecimal(coordMatcher.group(2));

                // 驗證範圍（台灣的經緯度範圍）
                if (lat.compareTo(new BigDecimal("21")) > 0 && lat.compareTo(new BigDecimal("26")) < 0 &&
                    lng.compareTo(new BigDecimal("119")) > 0 && lng.compareTo(new BigDecimal("122")) < 0) {
                    sight.setLatitude(lat);
                    sight.setLongitude(lng);
                }
            } catch (Exception e) {
                logger.debug("經緯度解析失敗", e);
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
