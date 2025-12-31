package com.example.crawler;

import com.example.entity.Festival;
import com.example.entity.Region;
import com.example.repository.FestivalRepository;
import com.example.repository.RegionRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 台灣觀光局活動爬蟲
 * 爬取 https://www.taiwan.net.tw/m1.aspx?sNo=0001019 的節慶活動資訊
 */
@Component
public class TaiwanNetFestivalCrawler {

    private static final Logger logger = LoggerFactory.getLogger(TaiwanNetFestivalCrawler.class);

    private static final String BASE_URL = "https://www.taiwan.net.tw";
    private static final String LIST_URL = BASE_URL + "/m1.aspx?sNo=0001019";

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private RegionRepository regionRepository;

    /**
     * 爬取所有分頁的活動列表
     * @param maxPages 最多爬取幾頁（0 表示爬取所有頁）
     * @param fetchDetails 是否爬取詳細頁面以補充完整資訊
     * @return 爬取到的活動列表
     */
    public List<Festival> crawlAllPages(int maxPages, boolean fetchDetails) {
        List<Festival> allFestivals = new ArrayList<>();

        try {
            // 先爬取第一頁以獲取總頁數
            Document firstPage = fetchDocument(LIST_URL);
            if (firstPage == null) {
                logger.error("無法獲取第一頁");
                return allFestivals;
            }

            // 解析第一頁的活動
            List<Festival> firstPageFestivals = parseListPage(firstPage);
            allFestivals.addAll(firstPageFestivals);
            logger.info("第 1 頁：爬取到 {} 個活動", firstPageFestivals.size());

            // 獲取總頁數
            int totalPages = getTotalPages(firstPage);
            logger.info("總共 {} 頁", totalPages);

            // 決定實際要爬取的頁數
            int pagesToCrawl = (maxPages > 0 && maxPages < totalPages) ? maxPages : totalPages;

            // 爬取剩餘頁面
            for (int page = 2; page <= pagesToCrawl; page++) {
                String pageUrl = LIST_URL + "&page=" + page;

                try {
                    Document doc = fetchDocument(pageUrl);
                    if (doc != null) {
                        List<Festival> pageFestivals = parseListPage(doc);
                        allFestivals.addAll(pageFestivals);
                        logger.info("第 {} 頁：爬取到 {} 個活動", page, pageFestivals.size());
                    }

                    // 延遲避免過快
                    Thread.sleep(1000);

                } catch (Exception e) {
                    logger.error("爬取第 {} 頁失敗", page, e);
                }
            }

            logger.info("✅ 列表頁爬取完成，總共 {} 個活動", allFestivals.size());

            // 如果需要爬取詳細資訊
            if (fetchDetails) {
                logger.info("開始爬取詳細頁面...");
                enrichWithDetailPages(allFestivals);
            }

        } catch (Exception e) {
            logger.error("❌ 爬取失敗", e);
        }

        return allFestivals;
    }

    /**
     * 解析列表頁面，提取活動基本資訊
     */
    private List<Festival> parseListPage(Document doc) {
        List<Festival> festivals = new ArrayList<>();

        Elements listItems = doc.select("ul.columnBlock-list > li");

        for (Element li : listItems) {
            try {
                Festival festival = new Festival();

                // 1. 活動名稱 & 詳細頁 URL
                Element titleLink = li.selectFirst("a.columnBlock-title");
                if (titleLink != null) {
                    festival.setFestivalName(titleLink.text().trim());

                    // 提取 lid 參數
                    String href = titleLink.attr("href");
                    festival.setSourceUrl(BASE_URL + "/" + href);

                    Pattern lidPattern = Pattern.compile("lid=(\\d+)");
                    Matcher lidMatcher = lidPattern.matcher(href);
                    if (lidMatcher.find()) {
                        festival.setEventId(lidMatcher.group(1));
                    }
                }

                // 2. 分類標籤
                Element tag = li.selectFirst(".hashtag span");
                if (tag != null) {
                    festival.setCategory(tag.text().trim());
                }

                // 3. 日期
                Element dateSpan = li.selectFirst("span.date");
                if (dateSpan != null) {
                    String dateString = dateSpan.text().trim();
                    festival.setDateString(dateString);
                    parseDateRange(dateString, festival);
                }

                // 4. 簡短描述
                Element descP = li.selectFirst(".columnBlock-info > p");
                if (descP != null) {
                    festival.setDescription(descP.text().trim());
                }

                // 5. 照片
                Element img = li.selectFirst("figure img[src]");
                if (img != null) {
                    String photoUrl = img.attr("abs:src");
                    festival.setPhotoUrl(photoUrl);
                }

                festivals.add(festival);

            } catch (Exception e) {
                logger.error("解析活動失敗", e);
            }
        }

        return festivals;
    }

    /**
     * 爬取詳細頁面資訊（補充完整資訊）
     */
    public Festival crawlDetailPage(String url) {
        try {
            Document doc = fetchDocument(url);
            if (doc == null) {
                return null;
            }

            // 先查詢資料庫是否已存在
            Optional<Festival> existing = festivalRepository.findBySourceUrl(url);
            Festival festival = existing.orElse(new Festival());

            festival.setSourceUrl(url);

            // 解析詳細資訊
            Elements infoItems = doc.select("dl.info-table dt, dl.info-table dd");

            String currentKey = null;
            for (Element elem : infoItems) {
                if (elem.tagName().equals("dt")) {
                    currentKey = elem.text().replace("：", "").trim();
                } else if (elem.tagName().equals("dd") && currentKey != null) {
                    String value = elem.text().trim();

                    switch (currentKey) {
                        case "活動時間":
                            festival.setDateString(value);
                            parseDateRange(value, festival);
                            break;

                        case "主辦單位":
                            festival.setContactInfo(value);
                            break;

                        case "地址":
                            // 可能有多個地址，取第一個
                            Elements addressLinks = elem.select("a");
                            if (!addressLinks.isEmpty()) {
                                String firstAddress = addressLinks.first().text().trim();
                                festival.setAddress(firstAddress);

                                // 從地址解析 zone 和 regionId
                                parseAddressInfo(firstAddress, festival);
                            }
                            break;

                        case "網站連結":
                            Element websiteLink = elem.selectFirst("a");
                            if (websiteLink != null) {
                                festival.setOfficialWebsite(websiteLink.attr("href"));
                            }
                            break;
                    }

                    currentKey = null;
                }
            }

            return festival;

        } catch (Exception e) {
            logger.error("爬取詳細頁失敗: {}", url, e);
            return null;
        }
    }

    /**
     * 從地址解析 zone（行政區）和 regionId（縣市 ID）
     * 例如：「新北市板橋區新北市市民廣場」→ zone="板橋區", regionId=2
     */
    private void parseAddressInfo(String address, Festival festival) {
        try {
            // 正則：匹配「XX市/縣 + XX區/鄉/鎮/市」
            Pattern pattern = Pattern.compile("([^市縣]+[市縣])([^區鄉鎮市]+[區鄉鎮市])");
            Matcher matcher = pattern.matcher(address);

            if (matcher.find()) {
                String cityName = matcher.group(1);  // 例：「新北市」
                String zoneName = matcher.group(2);  // 例：「板橋區」

                festival.setZone(zoneName);

                // 根據縣市名稱查詢 regionId
                // 處理「臺」與「台」的繁簡問題
                String normalizedCityName = cityName.replace("臺", "台");

                Optional<Region> region = regionRepository.findByName(normalizedCityName);
                if (region.isPresent()) {
                    festival.setRegionId(region.get().getId());
                    logger.debug("解析地址: {} → zone={}, regionId={}", address, zoneName, region.get().getId());
                } else {
                    logger.warn("找不到縣市: {} (已標準化為: {})", cityName, normalizedCityName);
                }
            }

        } catch (Exception e) {
            logger.warn("地址解析失敗: {}", address);
        }
    }

    /**
     * 批次爬取詳細頁面以補充完整資訊
     */
    private void enrichWithDetailPages(List<Festival> festivals) {
        int count = 0;
        for (Festival festival : festivals) {
            try {
                if (festival.getSourceUrl() != null) {
                    // 爬取詳細頁
                    Festival detailFestival = crawlDetailPage(festival.getSourceUrl());

                    if (detailFestival != null) {
                        // 合併資訊（列表頁 + 詳細頁）
                        mergeFestivalInfo(festival, detailFestival);
                        count++;
                    }

                    // 延遲避免過快
                    Thread.sleep(1000);

                    // 每 10 筆顯示進度
                    if (count % 10 == 0) {
                        logger.info("進度: {}/{}", count, festivals.size());
                    }
                }
            } catch (Exception e) {
                logger.error("爬取詳細頁失敗: {}", festival.getSourceUrl(), e);
            }
        }

        logger.info("✅ 詳細頁爬取完成，成功 {}/{} 筆", count, festivals.size());
    }

    /**
     * 合併列表頁和詳細頁的資訊
     */
    private void mergeFestivalInfo(Festival base, Festival detail) {
        // 詳細頁的資訊優先，但如果為空則保留列表頁的資訊
        if (detail.getAddress() != null && !detail.getAddress().isEmpty()) {
            base.setAddress(detail.getAddress());
        }
        if (detail.getZone() != null && !detail.getZone().isEmpty()) {
            base.setZone(detail.getZone());
        }
        if (detail.getRegionId() != null) {
            base.setRegionId(detail.getRegionId());
        }
        if (detail.getOfficialWebsite() != null && !detail.getOfficialWebsite().isEmpty()) {
            base.setOfficialWebsite(detail.getOfficialWebsite());
        }
        if (detail.getContactInfo() != null && !detail.getContactInfo().isEmpty()) {
            base.setContactInfo(detail.getContactInfo());
        }
    }

    /**
     * 儲存或更新活動（根據 sourceUrl 去重）
     */
    public Festival saveOrUpdate(Festival festival) {
        try {
            Optional<Festival> existing = festivalRepository.findBySourceUrl(festival.getSourceUrl());

            if (existing.isPresent()) {
                // 更新現有資料
                Festival existingFestival = existing.get();
                festival.setId(existingFestival.getId());
                festival.setCreatedAt(existingFestival.getCreatedAt());
                logger.info("更新活動: {}", festival.getFestivalName());
            } else {
                logger.info("新增活動: {}", festival.getFestivalName());
            }

            return festivalRepository.save(festival);

        } catch (Exception e) {
            logger.error("儲存活動失敗: {}", festival.getFestivalName(), e);
            return null;
        }
    }

    /**
     * 批次儲存
     */
    public List<Festival> saveOrUpdateBatch(List<Festival> festivals) {
        List<Festival> savedFestivals = new ArrayList<>();

        for (Festival festival : festivals) {
            Festival saved = saveOrUpdate(festival);
            if (saved != null) {
                savedFestivals.add(saved);
            }
        }

        logger.info("批次儲存完成：成功 {} 筆，失敗 {} 筆",
                savedFestivals.size(), festivals.size() - savedFestivals.size());

        return savedFestivals;
    }

    /**
     * 解析日期字串（例：2025-11-14~2025-12-28）
     */
    private void parseDateRange(String dateString, Festival festival) {
        try {
            // 格式: 2025-11-14~2025-12-28
            Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})~(\\d{4}-\\d{2}-\\d{2})");
            Matcher matcher = pattern.matcher(dateString);

            if (matcher.find()) {
                String startDateStr = matcher.group(1);
                String endDateStr = matcher.group(2);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                festival.setStartDate(LocalDate.parse(startDateStr, formatter));
                festival.setEndDate(LocalDate.parse(endDateStr, formatter));
            } else {
                // 嘗試單一日期格式
                Pattern singlePattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
                Matcher singleMatcher = singlePattern.matcher(dateString);
                if (singleMatcher.find()) {
                    LocalDate date = LocalDate.parse(singleMatcher.group(1), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    festival.setStartDate(date);
                    festival.setEndDate(date);
                }
            }

        } catch (Exception e) {
            logger.warn("日期解析失敗: {}", dateString);
        }
    }

    /**
     * 獲取總頁數
     */
    private int getTotalPages(Document doc) {
        try {
            // 從 "目前頁次：1/16" 提取總頁數
            Elements metaItems = doc.select("div.listMeta div.side-left div.listMeta-item span");

            for (Element span : metaItems) {
                String text = span.text().trim();
                // 尋找格式為 "目前頁次：1/16" 的文字
                if (text.startsWith("目前頁次：")) {
                    // 提取 "1/16" 中的 16
                    String pageInfo = text.substring(text.indexOf("：") + 1); // "1/16"
                    if (pageInfo.contains("/")) {
                        String[] parts = pageInfo.split("/");
                        if (parts.length == 2) {
                            int totalPages = Integer.parseInt(parts[1].trim());
                            logger.info("成功解析總頁數: {}", totalPages);
                            return totalPages;
                        }
                    }
                }
            }

            logger.warn("無法找到頁次資訊，使用預設值 1");
            return 1;

        } catch (Exception e) {
            logger.error("獲取總頁數失敗", e);
            return 1;
        }
    }

    /**
     * 獲取網頁 Document
     */
    private Document fetchDocument(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
        } catch (Exception e) {
            logger.error("無法獲取網頁: {}", url, e);
            return null;
        }
    }
}
