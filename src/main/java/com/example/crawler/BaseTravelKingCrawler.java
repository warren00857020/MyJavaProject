package com.example.crawler;

import com.example.crawler.config.CrawlerConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TravelKing 爬蟲通用基類
 * 提供通用的爬取方法和錯誤處理機制
 *
 * @param <T> 要爬取的實體類型（Sight, Food, Festival 等）
 */
public abstract class BaseTravelKingCrawler<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 獲取列表頁 URL
     * @param county 縣市 slug (例：taipei)
     * @param city 城市 slug (例：taipei-city)
     * @return 列表頁 URL
     */
    protected abstract String getListUrl(String county, String city);

    /**
     * 解析詳細頁面，提取資料
     * @param doc 詳細頁面的 Document
     * @param url 詳細頁面的 URL
     * @return 解析後的實體物件
     */
    protected abstract T parseDetailPage(Document doc, String url);

    /**
     * 儲存單筆資料
     * @param item 要儲存的實體
     */
    protected abstract void saveItem(T item);

    /**
     * 批次儲存資料
     * @param items 要儲存的實體列表
     */
    protected abstract void saveBatch(List<T> items);

    /**
     * 爬取單一城市的資料
     * @param county 縣市 slug
     * @param city 城市 slug
     * @return 爬取到的資料列表
     */
    public List<T> crawlByCity(String county, String city) {
        logger.info("開始爬取：{}/{}", county, city);
        List<T> results = new ArrayList<>();

        try {
            // 1. 獲取列表頁
            String listUrl = getListUrl(county, city);
            Document listDoc = fetchDocument(listUrl);

            if (listDoc == null) {
                logger.error("無法獲取列表頁：{}", listUrl);
                return results;
            }

            // 2. 提取所有詳細頁連結
            List<String> detailUrls = extractDetailLinks(listDoc);
            logger.info("找到 {} 個景點連結", detailUrls.size());

            // 3. 爬取每個詳細頁
            for (int i = 0; i < detailUrls.size(); i++) {
                String detailUrl = detailUrls.get(i);
                logger.info("爬取進度：{}/{} - {}", i + 1, detailUrls.size(), detailUrl);

                try {
                    Document detailDoc = fetchDocument(detailUrl);
                    if (detailDoc != null) {
                        T item = parseDetailPage(detailDoc, detailUrl);
                        if (item != null) {
                            results.add(item);
                        }
                    }
                } catch (Exception e) {
                    logger.error("爬取詳細頁失敗：{}", detailUrl, e);
                }

                // 隨機延遲，避免被封鎖
                randomDelay();
            }

            logger.info("完成爬取：{}/{}，成功 {} 筆", county, city, results.size());

        } catch (Exception e) {
            logger.error("爬取失敗：{}/{}", county, city, e);
        }

        return results;
    }

    /**
     * 爬取整個縣市的資料
     * @param county 縣市 slug
     * @param cities 該縣市下的城市列表
     * @return 爬取到的資料列表
     */
    public List<T> crawlByCounty(String county, List<String> cities) {
        logger.info("開始爬取縣市：{}", county);
        List<T> allResults = new ArrayList<>();

        for (String city : cities) {
            List<T> cityResults = crawlByCity(county, city);
            allResults.addAll(cityResults);

            // 批次儲存
            if (!cityResults.isEmpty()) {
                saveBatch(cityResults);
            }
        }

        logger.info("完成縣市爬取：{}，總共 {} 筆", county, allResults.size());
        return allResults;
    }

    /**
     * 從列表頁提取所有詳細頁連結
     * @param listDoc 列表頁 Document
     * @return 詳細頁 URL 列表
     */
    protected List<String> extractDetailLinks(Document listDoc) {
        List<String> links = new ArrayList<>();

        // TravelKing 的景點連結格式：/tourguide/{county}/scenery{id}.html
        // 或 /tourguide/scenery{id}.html
        Elements linkElements = listDoc.select("a[href*=scenery]");

        for (Element link : linkElements) {
            String href = link.attr("href");

            // 處理相對路徑
            if (href.startsWith("/")) {
                href = CrawlerConfig.BASE_URL + href;
            } else if (!href.startsWith("http")) {
                href = CrawlerConfig.BASE_URL + "/" + href;
            }

            // 避免重複
            if (!links.contains(href)) {
                links.add(href);
            }
        }

        return links;
    }

    /**
     * 獲取網頁 Document（帶重試機制）
     * @param url 網頁 URL
     * @return Document 物件，失敗返回 null
     */
    protected Document fetchDocument(String url) {
        for (int attempt = 0; attempt < CrawlerConfig.RETRY_LIMIT; attempt++) {
            try {
                logger.debug("嘗試獲取：{} (第 {} 次)", url, attempt + 1);

                Document doc = Jsoup.connect(url)
                    .userAgent(CrawlerConfig.getRandomUserAgent())
                    .timeout(10000)
                    .get();

                return doc;

            } catch (IOException e) {
                logger.warn("獲取失敗 (第 {} 次)：{}", attempt + 1, url);

                // 最後一次嘗試才拋出錯誤
                if (attempt == CrawlerConfig.RETRY_LIMIT - 1) {
                    logger.error("達到重試上限，放棄：{}", url, e);
                    return null;
                }

                // 指數退避
                try {
                    Thread.sleep(1000 * (attempt + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * 隨機延遲（避免被網站封鎖）
     */
    protected void randomDelay() {
        try {
            long delay = CrawlerConfig.getRandomDelay();
            logger.debug("延遲 {} 毫秒", delay);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("延遲被中斷");
        }
    }
}
