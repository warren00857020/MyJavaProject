package com.example.crawler;

import com.example.entity.Sight;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * TDX (運輸資料流通服務) 景點爬蟲
 * 資料來源：交通部 TDX 平台
 * API 文件：https://tdx.transportdata.tw/api-service/swagger
 */
@Component
public class TdxScenicSpotCrawler {

    private static final String AUTH_URL = "https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token";
    private static final String API_BASE_URL = "https://tdx.transportdata.tw/api/basic/v2/Tourism/ScenicSpot";

    private String clientId;
    private String clientSecret;
    private String accessToken;
    private long tokenExpiryTime;

    /**
     * 設定 TDX API 認證資訊
     */
    public void setCredentials(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * 取得 Access Token
     */
    private String getAccessToken() throws Exception {
        // 如果 token 還有效，直接返回
        if (accessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return accessToken;
        }

        URL url = new URL(AUTH_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

        String requestBody = "grant_type=client_credentials";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            JSONObject json = new JSONObject(response.toString());
            accessToken = json.getString("access_token");
            int expiresIn = json.getInt("expires_in");
            tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000) - 60000; // 提前1分鐘過期

            System.out.println("✅ TDX Access Token 取得成功");
            return accessToken;
        } else {
            throw new RuntimeException("Failed to get access token: " + responseCode);
        }
    }

    /**
     * 爬取指定城市的景點資料
     */
    public List<Sight> crawlScenicSpots(String city, Integer top) throws Exception {
        String token = getAccessToken();

        // 建構 API URL
        String apiUrl = API_BASE_URL + "/" + city;
        if (top != null && top > 0) {
            apiUrl += "?$top=" + top;
        }

        System.out.println("🔍 正在爬取 " + city + " 的景點資料...");
        System.out.println("📡 API URL: " + apiUrl);

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            return parseScenicSpots(response.toString());
        } else {
            throw new RuntimeException("API request failed: " + responseCode);
        }
    }

    /**
     * 解析 TDX API 回應的 JSON 資料
     */
    private List<Sight> parseScenicSpots(String jsonResponse) {
        List<Sight> sights = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonResponse);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = jsonArray.getJSONObject(i);

            Sight sight = new Sight();

            // 基本資訊
            sight.setSightName(json.optString("ScenicSpotName", ""));
            sight.setDescription(json.optString("DescriptionDetail",
                                json.optString("Description", "")));

            // 地址資訊
            sight.setAddress(json.optString("Address", ""));

            // 聯絡資訊
            sight.setPhone(json.optString("Phone", ""));

            // 網站
            if (json.has("WebsiteUrl")) {
                sight.setOfficialWebsite(json.optString("WebsiteUrl", ""));
            }

            // 座標
            if (json.has("Position")) {
                JSONObject position = json.getJSONObject("Position");
                double lat = position.optDouble("PositionLat", 0.0);
                double lon = position.optDouble("PositionLon", 0.0);
                if (lat != 0.0) {
                    sight.setLatitude(new java.math.BigDecimal(String.valueOf(lat)));
                }
                if (lon != 0.0) {
                    sight.setLongitude(new java.math.BigDecimal(String.valueOf(lon)));
                }
            }

            // 營業時間
            sight.setOpeningHours(json.optString("OpenTime", ""));

            // 門票資訊
            if (json.has("TicketInfo")) {
                sight.setTicketPrice(json.optString("TicketInfo", ""));
            }

            // 圖片
            if (json.has("Picture")) {
                JSONObject picture = json.getJSONObject("Picture");
                if (picture.has("PictureUrl1")) {
                    sight.setPhotoURL(picture.optString("PictureUrl1", ""));
                }
            }

            // 地區（從 Class 欄位取得，存入 zone）
            if (json.has("Class")) {
                sight.setZone(json.optString("Class", ""));
            }

            // 分類（從 Class 欄位取得）
            if (json.has("Class1") || json.has("Class2")) {
                String class1 = json.optString("Class1", "");
                String class2 = json.optString("Class2", "");
                sight.setCategory(class1 + (class2.isEmpty() ? "" : "," + class2));
            }

            // 資料來源 URL
            sight.setSourceUrl("TDX API");

            sights.add(sight);

            System.out.println("  ✅ " + sight.getSightName());
        }

        System.out.println("📊 成功解析 " + sights.size() + " 筆景點資料");
        return sights;
    }

    /**
     * 爬取所有支援的城市
     * 台灣主要城市代碼
     */
    public static final String[] SUPPORTED_CITIES = {
        "Taipei",      // 台北市
        "NewTaipei",   // 新北市
        "Taoyuan",     // 桃園市
        "Taichung",    // 台中市
        "Tainan",      // 台南市
        "Kaohsiung",   // 高雄市
        "Keelung",     // 基隆市
        "Hsinchu",     // 新竹市
        "HsinchuCounty", // 新竹縣
        "MiaoliCounty",  // 苗栗縣
        "ChanghuaCounty", // 彰化縣
        "NantouCounty",   // 南投縣
        "YunlinCounty",   // 雲林縣
        "ChiayiCounty",   // 嘉義縣
        "Chiayi",         // 嘉義市
        "PingtungCounty", // 屏東縣
        "YilanCounty",    // 宜蘭縣
        "HualienCounty",  // 花蓮縣
        "TaitungCounty",  // 台東縣
        "PenghuCounty",   // 澎湖縣
        "KinmenCounty",   // 金門縣
        "LienchiangCounty" // 連江縣
    };
}
