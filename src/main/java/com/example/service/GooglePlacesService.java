package com.example.service;

import com.example.entity.Food;
import com.example.entity.Sight;
import com.example.repository.FoodRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Google Places API 服務
 * 用於搜尋和儲存餐廳資料
 */
@Service
public class GooglePlacesService {

    private static final Logger logger = LoggerFactory.getLogger(GooglePlacesService.class);

    @Value("${google.places.api.key}")
    private String apiKey;

    @Autowired
    private FoodRepository foodRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    // 在 Bean 初始化後檢查 API Key
    @jakarta.annotation.PostConstruct
    public void init() {
        logger.info("Google Places API Key 已載入（前10字元）: {}...",
            apiKey != null && apiKey.length() > 10 ? apiKey.substring(0, 10) : "未設定");
        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("警告：Google Places API Key 未設定！");
        }
    }

    // API URLs
    private static final String NEARBY_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private static final String TEXT_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json";
    private static final String PLACE_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json";

    /**
     * 搜尋景點周邊的餐廳
     * @param sight 景點
     * @param radius 半徑（公尺）
     * @param maxResults 最多結果數
     * @return 餐廳列表
     */
    public List<Food> searchNearbyRestaurants(Sight sight, int radius, int maxResults) {
        if (sight.getLatitude() == null || sight.getLongitude() == null) {
            logger.warn("景點 {} 沒有座標資訊，無法搜尋周邊餐廳", sight.getSightName());
            return new ArrayList<>();
        }

        return searchNearby(
            sight.getLatitude().doubleValue(),
            sight.getLongitude().doubleValue(),
            radius,
            "restaurant",
            maxResults
        );
    }

    /**
     * 以座標為中心搜尋附近的餐廳
     * @param lat 緯度
     * @param lng 經度
     * @param radius 半徑（公尺）
     * @param type 類型（restaurant, cafe, food 等）
     * @param maxResults 最多結果數
     * @return 餐廳列表
     */
    public List<Food> searchNearby(double lat, double lng, int radius, String type, int maxResults) {
        List<Food> results = new ArrayList<>();

        try {
            String url = String.format(
                "%s?location=%f,%f&radius=%d&type=%s&language=zh-TW&key=%s",
                NEARBY_SEARCH_URL, lat, lng, radius, type, apiKey
            );

            logger.info("呼叫 Google Places API: location=({}, {}), radius={}, type={}", lat, lng, radius, type);

            String response = restTemplate.getForObject(url, String.class);
            JSONObject jsonResponse = new JSONObject(response);

            String status = jsonResponse.getString("status");
            logger.info("API 回應狀態: {}", status);

            if (!"OK".equals(status) && !"ZERO_RESULTS".equals(status)) {
                logger.error("Google Places API 錯誤: {}", status);
                if (jsonResponse.has("error_message")) {
                    logger.error("錯誤訊息: {}", jsonResponse.getString("error_message"));
                }
                return results;
            }

            if ("ZERO_RESULTS".equals(status)) {
                logger.info("搜尋結果為空");
                return results;
            }

            JSONArray placesArray = jsonResponse.getJSONArray("results");
            int count = Math.min(placesArray.length(), maxResults);

            for (int i = 0; i < count; i++) {
                try {
                    JSONObject place = placesArray.getJSONObject(i);
                    Food food = parsePlace(place);
                    if (food != null) {
                        results.add(food);
                    }
                } catch (Exception e) {
                    logger.error("解析餐廳資料失敗", e);
                }
            }

            logger.info("成功解析 {} 間餐廳", results.size());

        } catch (Exception e) {
            logger.error("呼叫 Google Places API 失敗", e);
        }

        return results;
    }

    /**
     * 以文字搜尋餐廳
     * @param query 搜尋關鍵字（如："台北市信義區 餐廳"）
     * @param maxResults 最多結果數
     * @return 餐廳列表
     */
    public List<Food> searchByText(String query, int maxResults) {
        List<Food> results = new ArrayList<>();

        try {
            // 使用和測試端點相同的 URL 構建方式
            // 注意：不要對整個 query 做 URLEncoder.encode，而是直接用 %20 替換空格
            String formattedQuery = query.replace(" ", "%20");

            String url = String.format(
                "%s?query=%s&language=zh-TW&key=%s",
                TEXT_SEARCH_URL, formattedQuery, apiKey
            );

            logger.info("文字搜尋: {}", query);
            logger.info("完整 API URL: {}", url);

            String response = restTemplate.getForObject(url, String.class);
            logger.info("API 回應長度: {} 字元", response != null ? response.length() : 0);

            JSONObject jsonResponse = new JSONObject(response);

            String status = jsonResponse.getString("status");
            logger.info("API 狀態: {}", status);

            if (!"OK".equals(status) && !"ZERO_RESULTS".equals(status)) {
                logger.error("Google Places API 錯誤: {}", status);
                if (jsonResponse.has("error_message")) {
                    logger.error("錯誤訊息: {}", jsonResponse.getString("error_message"));
                }
                return results;
            }

            if ("ZERO_RESULTS".equals(status)) {
                logger.warn("搜尋結果為空");
                return results;
            }

            JSONArray placesArray = jsonResponse.getJSONArray("results");
            int count = Math.min(placesArray.length(), maxResults);

            for (int i = 0; i < count; i++) {
                try {
                    JSONObject place = placesArray.getJSONObject(i);
                    Food food = parsePlace(place);
                    if (food != null) {
                        results.add(food);
                    }
                } catch (Exception e) {
                    logger.error("解析餐廳資料失敗", e);
                }
            }

            logger.info("成功解析 {} 間餐廳", results.size());

        } catch (Exception e) {
            logger.error("文字搜尋失敗", e);
        }

        return results;
    }

    /**
     * 解析 Google Places API 回傳的單一餐廳資料
     */
    private Food parsePlace(JSONObject place) {
        Food food = new Food();

        try {
            // 1. Place ID（最重要！）
            String placeId = place.getString("place_id");
            food.setPlaceId(placeId);

            // 2. 名稱
            String name = place.getString("name");
            food.setFoodName(name);

            // 3. 地址
            if (place.has("formatted_address")) {
                String address = place.getString("formatted_address");
                food.setAddress(address);

                // 提取行政區
                String zone = extractZoneFromAddress(address);
                food.setZone(zone);
            } else if (place.has("vicinity")) {
                String vicinity = place.getString("vicinity");
                food.setAddress(vicinity);
            }

            // 4. 座標
            if (place.has("geometry")) {
                JSONObject geometry = place.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");

                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");

                food.setLatitude(BigDecimal.valueOf(lat));
                food.setLongitude(BigDecimal.valueOf(lng));
            }

            // 5. 評分
            if (place.has("rating")) {
                food.setRating(place.getDouble("rating"));
            }

            // 6. 評價數量
            if (place.has("user_ratings_total")) {
                food.setUserRatingsTotal(place.getInt("user_ratings_total"));
            }

            // 7. 價格等級
            if (place.has("price_level")) {
                int priceLevel = place.getInt("price_level");
                food.setPriceLevel(priceLevel);
                food.setPriceRange(convertPriceLevel(priceLevel));
            }

            // 8. 營業狀態
            if (place.has("business_status")) {
                food.setBusinessStatus(place.getString("business_status"));
            }

            // 9. 類型陣列
            if (place.has("types")) {
                JSONArray typesArray = place.getJSONArray("types");
                String[] types = new String[typesArray.length()];
                for (int i = 0; i < typesArray.length(); i++) {
                    types[i] = typesArray.getString(i);
                }
                food.setGoogleTypes(types);

                // 轉換為分類
                String category = convertTypesToCategory(types);
                food.setCategory(category);
            }

            // 10. 照片參考
            if (place.has("photos")) {
                JSONArray photosArray = place.getJSONArray("photos");
                String[] photoRefs = new String[photosArray.length()];
                for (int i = 0; i < photosArray.length(); i++) {
                    JSONObject photo = photosArray.getJSONObject(i);
                    photoRefs[i] = photo.getString("photo_reference");
                }
                food.setPhotoReferences(photoRefs);

                // 設定第一張照片 URL
                if (photoRefs.length > 0) {
                    String photoUrl = generatePhotoUrl(photoRefs[0], 800);
                    food.setPhotoUrl(photoUrl);
                }
            }

            // 11. 營業時間（簡化版）
            if (place.has("opening_hours")) {
                JSONObject openingHours = place.getJSONObject("opening_hours");
                food.setOpeningHours(openingHours.toString());
            }

            // 12. 預設值
            food.setViewCount(0);
            food.setIsVerified(false);

            return food;

        } catch (Exception e) {
            logger.error("解析餐廳資料時發生錯誤: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 從地址提取行政區
     */
    private String extractZoneFromAddress(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }

        // 常見行政區模式：台北市信義區、新北市板橋區等
        String[] patterns = {
            "市(\\S+區)", "縣(\\S+鄉)", "縣(\\S+鎮)", "縣(\\S+市)"
        };

        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(address);
            if (m.find()) {
                return m.group(1);
            }
        }

        return null;
    }

    /**
     * 轉換 Google 價格等級為字串
     */
    private String convertPriceLevel(int priceLevel) {
        switch (priceLevel) {
            case 0: return "免費";
            case 1: return "$";
            case 2: return "$$";
            case 3: return "$$$";
            case 4: return "$$$$";
            default: return null;
        }
    }

    /**
     * 轉換 Google Types 為分類
     */
    private String convertTypesToCategory(String[] types) {
        for (String type : types) {
            switch (type) {
                case "restaurant": return "餐廳";
                case "cafe": return "咖啡廳";
                case "bar": return "酒吧";
                case "bakery": return "烘焙坊";
                case "meal_takeaway": return "外帶";
                case "food": return "美食";
                default: continue;
            }
        }
        return "餐廳"; // 預設
    }

    /**
     * 生成照片 URL
     */
    private String generatePhotoUrl(String photoReference, int maxWidth) {
        return String.format(
            "https://maps.googleapis.com/maps/api/place/photo?maxwidth=%d&photo_reference=%s&key=%s",
            maxWidth, photoReference, apiKey
        );
    }

    /**
     * 獲取餐廳詳細資訊（包含完整營業時間）
     * @param placeId Google Place ID
     * @return 完整的餐廳資訊，包含營業時間
     */
    public Food getPlaceDetails(String placeId) {
        try {
            String url = String.format(
                "%s?place_id=%s&fields=name,rating,user_ratings_total,price_level,formatted_address,geometry,formatted_phone_number,website,opening_hours,business_status,types,photos&language=zh-TW&key=%s",
                PLACE_DETAILS_URL, placeId, apiKey
            );

            logger.info("獲取餐廳詳細資訊: placeId={}", placeId);

            String response = restTemplate.getForObject(url, String.class);
            JSONObject jsonResponse = new JSONObject(response);

            String status = jsonResponse.getString("status");
            logger.info("Place Details API 狀態: {}", status);

            if (!"OK".equals(status)) {
                logger.error("Place Details API 錯誤: {}", status);
                if (jsonResponse.has("error_message")) {
                    logger.error("錯誤訊息: {}", jsonResponse.getString("error_message"));
                }
                return null;
            }

            JSONObject result = jsonResponse.getJSONObject("result");

            // 手動解析 Place Details API 回應（不使用 parsePlace，因為結構不同）
            Food food = new Food();

            // 設定 placeId（Place Details API 不會返回 place_id）
            food.setPlaceId(placeId);

            // 名稱
            if (result.has("name")) {
                food.setFoodName(result.getString("name"));
            }

            // 評分
            if (result.has("rating")) {
                food.setRating(result.getDouble("rating"));
            }

            // 評價數量
            if (result.has("user_ratings_total")) {
                food.setUserRatingsTotal(result.getInt("user_ratings_total"));
            }

            // 價格等級
            if (result.has("price_level")) {
                int priceLevel = result.getInt("price_level");
                food.setPriceLevel(priceLevel);
                food.setPriceRange(convertPriceLevel(priceLevel));
            }

            // 地址
            if (result.has("formatted_address")) {
                String address = result.getString("formatted_address");
                food.setAddress(address);

                // 提取行政區
                String zone = extractZoneFromAddress(address);
                food.setZone(zone);
            }

            // 座標
            if (result.has("geometry")) {
                JSONObject geometry = result.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
                food.setLatitude(BigDecimal.valueOf(location.getDouble("lat")));
                food.setLongitude(BigDecimal.valueOf(location.getDouble("lng")));
            }

            // 完整營業時間（這是最重要的！）
            if (result.has("opening_hours")) {
                JSONObject openingHours = result.getJSONObject("opening_hours");
                food.setOpeningHours(openingHours.toString());
                logger.info("完整營業時間已獲取: {} 字元", openingHours.toString().length());
            }

            // 電話號碼
            if (result.has("formatted_phone_number")) {
                food.setPhone(result.getString("formatted_phone_number"));
                logger.info("電話: {}", food.getPhone());
            }

            // 網站
            if (result.has("website")) {
                food.setOfficialWebsite(result.getString("website"));
                logger.info("網站: {}", food.getOfficialWebsite());
            }

            // 營業狀態
            if (result.has("business_status")) {
                food.setBusinessStatus(result.getString("business_status"));
            }

            // 類型陣列
            if (result.has("types")) {
                JSONArray typesArray = result.getJSONArray("types");
                String[] types = new String[typesArray.length()];
                for (int i = 0; i < typesArray.length(); i++) {
                    types[i] = typesArray.getString(i);
                }
                food.setGoogleTypes(types);
                food.setCategory(convertTypesToCategory(types));
            }

            // 照片
            if (result.has("photos")) {
                JSONArray photosArray = result.getJSONArray("photos");
                String[] photoRefs = new String[photosArray.length()];
                for (int i = 0; i < photosArray.length(); i++) {
                    JSONObject photo = photosArray.getJSONObject(i);
                    photoRefs[i] = photo.getString("photo_reference");
                }
                food.setPhotoReferences(photoRefs);

                if (photoRefs.length > 0) {
                    food.setPhotoUrl(generatePhotoUrl(photoRefs[0], 800));
                }
            }

            food.setViewCount(0);
            food.setIsVerified(false);

            logger.info("成功解析餐廳詳細資訊: {}", food.getFoodName());
            return food;

        } catch (Exception e) {
            logger.error("獲取餐廳詳細資訊失敗: placeId={}", placeId, e);
            return null;
        }
    }

    /**
     * 為餐廳補充詳細資訊（營業時間、電話、網站）
     * @param food 基本餐廳資訊
     * @return 補充完整資訊後的餐廳物件
     */
    public Food enrichWithDetails(Food food) {
        if (food.getPlaceId() == null) {
            logger.warn("餐廳 {} 沒有 placeId，無法獲取詳細資訊", food.getFoodName());
            return food;
        }

        try {
            Food detailedFood = getPlaceDetails(food.getPlaceId());

            if (detailedFood != null) {
                // 補充詳細資訊到原始物件
                food.setOpeningHours(detailedFood.getOpeningHours());
                food.setPhone(detailedFood.getPhone());
                food.setOfficialWebsite(detailedFood.getOfficialWebsite());

                logger.info("已補充餐廳 {} 的詳細資訊", food.getFoodName());
            }

        } catch (Exception e) {
            logger.error("補充餐廳 {} 詳細資訊失敗", food.getFoodName(), e);
        }

        return food;
    }

    /**
     * 儲存或更新餐廳資料
     */
    public Food saveOrUpdateFood(Food food) {
        // 檢查是否已存在（根據 placeId）
        Food existing = foodRepository.findByPlaceId(food.getPlaceId());

        if (existing != null) {
            // 更新現有資料（保留 ID 和建立時間）
            food.setId(existing.getId());
            food.setCreatedAt(existing.getCreatedAt());

            logger.info("更新餐廳: {} (placeId: {})", food.getFoodName(), food.getPlaceId());
        } else {
            logger.info("新增餐廳: {} (placeId: {})", food.getFoodName(), food.getPlaceId());
        }

        return foodRepository.save(food);
    }

    /**
     * 批次儲存餐廳資料
     */
    public List<Food> saveOrUpdateBatch(List<Food> foods) {
        List<Food> savedFoods = new ArrayList<>();

        for (Food food : foods) {
            try {
                Food saved = saveOrUpdateFood(food);
                savedFoods.add(saved);
            } catch (Exception e) {
                logger.error("儲存餐廳失敗: {}", food.getFoodName(), e);
            }
        }

        logger.info("批次儲存完成：成功 {} 筆，失敗 {} 筆",
            savedFoods.size(), foods.size() - savedFoods.size());

        return savedFoods;
    }
}
