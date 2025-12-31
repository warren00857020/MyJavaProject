package com.example.crawler;

import com.example.crawler.config.CrawlerConfig;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TravelKing 爬蟲通用基類
 * 提供通用的爬取方法和錯誤處理機制
 *
 * @param <T> 要爬取的實體類型（Sight, Food, Festival 等）
 */
public abstract class BaseTravelKingCrawler<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // 儲存 cookies 以維持 session
    private Map<String, String> cookies = new java.util.HashMap<>();

    // 是否已經初始化 session（訪問過首頁）
    private boolean sessionInitialized = false;

    // 本次爬取使用的固定 User-Agent（必須保持一致）
    private String userAgent;

    /**
     * 獲取列表頁 URL
     * @param city 城市 slug (例：taipei-city, hualien-county)
     * @return 列表頁 URL
     */
    protected abstract String getListUrl(String city);

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
     * @param city 城市 slug (例：hualien-county)
     * @return 爬取到的資料列表
     */
    public List<T> crawlByCity(String city) {
        logger.info("開始爬取：{}", city);
        List<T> results = new ArrayList<>();

        try {
            // 1. 獲取列表頁
            String listUrl = getListUrl(city);
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

            logger.info("完成爬取：{}，成功 {} 筆", city, results.size());

        } catch (Exception e) {
            logger.error("爬取失敗：{}", city, e);
        }

        return results;
    }

    /**
     * 爬取多個城市的資料
     * @param cities 城市列表
     * @return 爬取到的資料列表
     */
    public List<T> crawlMultipleCities(List<String> cities) {
        logger.info("開始爬取 {} 個城市", cities.size());
        List<T> allResults = new ArrayList<>();

        for (String city : cities) {
            List<T> cityResults = crawlByCity(city);
            allResults.addAll(cityResults);

            // 批次儲存
            if (!cityResults.isEmpty()) {
                saveBatch(cityResults);
            }
        }

        logger.info("完成爬取，總共 {} 筆", allResults.size());
        return allResults;
    }

    /**
     * 從列表頁提取所有詳細頁連結
     * @param listDoc 列表頁 Document
     * @return 詳細頁 URL 列表
     */
    protected List<String> extractDetailLinks(Document listDoc) {
        List<String> links = new ArrayList<>();

        // 偵錯：輸出頁面標題
        logger.info("頁面標題：{}", listDoc.title());

        // 根據實際結構：在 <div class="box"> 裡的 <ul><li><a href="/tourguide/scenery{id}.html">
        Elements boxDivs = listDoc.select("div.box");
        logger.info("找到 {} 個 div.box", boxDivs.size());

        // 如果找不到 div.box，嘗試其他可能的結構
        if (boxDivs.isEmpty()) {
            logger.warn("找不到 div.box，嘗試直接搜尋所有 scenery 連結");

            // 直接搜尋所有包含 scenery 的連結
            Elements allSceneryLinks = listDoc.select("a[href*=scenery]");
            logger.info("找到 {} 個包含 scenery 的連結", allSceneryLinks.size());

            for (Element link : allSceneryLinks) {
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
                    logger.info("找到景點連結：{} -> {}", link.text(), href);
                }
            }
        } else {
            // 原本的邏輯
            for (Element box : boxDivs) {
                // 在每個 box 中找景點連結
                Elements linkElements = box.select("ul li a[href*=scenery]");
                logger.info("box 中找到 {} 個連結", linkElements.size());

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
                        logger.info("找到景點連結：{} -> {}", link.text(), href);
                    }
                }
            }
        }

        return links;
    }

    /**
     * 初始化 Session：先訪問首頁和 tourguide 主頁建立 Cookie
     */
    private void initializeSession() {
        if (sessionInitialized) {
            return;  // 已經初始化過，不需要重複
        }

        // 選擇一個 User-Agent 並在整個爬取過程中保持不變
        if (userAgent == null) {
            userAgent = CrawlerConfig.getRandomUserAgent();
            logger.info("🔧 選擇 User-Agent：{}", userAgent);
        }

        logger.info("🔧 初始化 Session：訪問首頁和 tourguide 主頁...");

        try {
            // 步驟 1：訪問首頁
            logger.info("步驟 1/2：訪問 https://www.travelking.com.tw/");
            Connection.Response homeResponse = Jsoup.connect("https://www.travelking.com.tw/")
                .userAgent(userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-TW,zh;q=0.9")
                .followRedirects(true)
                .timeout(15000)
                .execute();

            cookies.putAll(homeResponse.cookies());
            logger.info("✅ 首頁訪問成功，狀態碼：{}", homeResponse.statusCode());
            logger.info("獲得 Cookies：{}", cookies);

            // 等待 1-2 秒，模擬真實用戶行為
            Thread.sleep(1500);

            // 步驟 2：訪問 tourguide 主頁
            logger.info("步驟 2/2：訪問 https://www.travelking.com.tw/tourguide/taiwan/");
            Connection.Response tourguideResponse = Jsoup.connect("https://www.travelking.com.tw/tourguide/taiwan/")
                .userAgent(userAgent)  // 使用相同的 User-Agent
                .referrer("https://www.travelking.com.tw/")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-TW,zh;q=0.9")
                .cookies(cookies)
                .followRedirects(true)
                .timeout(15000)
                .execute();

            cookies.putAll(tourguideResponse.cookies());
            logger.info("✅ Tourguide 主頁訪問成功，狀態碼：{}", tourguideResponse.statusCode());
            logger.info("更新後的 Cookies：{}", cookies);

            sessionInitialized = true;
            logger.info("🎉 Session 初始化完成！");

            // 再等待 1-2 秒
            Thread.sleep(1500);

        } catch (Exception e) {
            logger.error("❌ Session 初始化失敗", e);
            // 即使失敗，也標記為已嘗試，避免無限重試
            sessionInitialized = true;
        }
    }

    /**
     * 獲取網頁 Document（帶重試機制）
     * @param url 網頁 URL
     * @return Document 物件，失敗返回 null
     */
    protected Document fetchDocument(String url) {
        // 在第一次訪問任何頁面前，先初始化 session
        initializeSession();

        for (int attempt = 0; attempt < CrawlerConfig.RETRY_LIMIT; attempt++) {
            try {
                logger.debug("嘗試獲取：{} (第 {} 次)", url, attempt + 1);

                Connection.Response response = Jsoup.connect(url)
                    .userAgent(userAgent)  // 使用固定的 User-Agent，不再隨機
                    .referrer("https://www.travelking.com.tw/tourguide/taiwan/")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "zh-TW,zh;q=0.9,en;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .cookies(cookies)
                    .followRedirects(true)
                    .ignoreHttpErrors(true)  // 忽略 HTTP 錯誤，讓我們看到實際內容
                    .timeout(CrawlerConfig.CONNECTION_TIMEOUT)  // 使用配置的超時時間
                    .maxBodySize(0)  // 不限制body大小
                    .execute();

                logger.info("HTTP 狀態碼：{}", response.statusCode());
                logger.info("Content-Type: {}", response.contentType());

                // 儲存新的 cookies
                cookies.putAll(response.cookies());

                Document doc = response.parse();

                // 檢查是否返回 404 頁面
                String title = doc.title();
                if (title.contains("404") || title.contains("Not Found")) {
                    logger.warn("收到 404 頁面，可能被反爬蟲阻擋：{}", url);
                    throw new IOException("收到 404 頁面");
                }

                logger.info("✅ 成功獲取頁面：{}", url);
                return doc;

            } catch (IOException e) {
                logger.warn("獲取失敗 (第 {} 次)：{} - {}", attempt + 1, url, e.getMessage());

                // 最後一次嘗試才記錄完整錯誤
                if (attempt == CrawlerConfig.RETRY_LIMIT - 1) {
                    logger.error("❌ 達到重試上限 ({} 次)，放棄：{}", CrawlerConfig.RETRY_LIMIT, url, e);
                    return null;
                }

                // 激進的指數退避策略
                // 第1次失敗：等 5 秒
                // 第2次失敗：等 10 秒
                // 第3次失敗：等 20 秒
                // 第4次失敗：等 30 秒
                try {
                    long baseWait = 5000;  // 5 秒基礎等待
                    long waitTime = baseWait * (long)Math.pow(2, attempt);
                    if (waitTime > 30000) waitTime = 30000;  // 最多等 30 秒

                    logger.warn("⏱️ 等待 {} 秒後重試... (指數退避策略)", waitTime / 1000);
                    Thread.sleep(waitTime);
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
