package com.example.crawler.config;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 縣市城市 URL 映射配置
 * TravelKing 使用特定的 URL slug 來表示縣市和城市
 */
@Component
public class CityMappingConfig {

    /**
     * 縣市代碼對應 TravelKing URL slug
     * 格式：資料庫 region code -> TravelKing URL county slug
     */
    public static final Map<String, String> REGION_TO_COUNTY_MAP = Map.ofEntries(
        Map.entry("TPE", "taipei"),           // 台北市
        Map.entry("TPH", "newtaipei"),        // 新北市
        Map.entry("TAO", "taoyuan"),          // 桃園市
        Map.entry("TXG", "taichung"),         // 台中市
        Map.entry("TNN", "tainan"),           // 台南市
        Map.entry("KHH", "kaohsiung"),        // 高雄市
        Map.entry("KEL", "keelung"),          // 基隆市
        Map.entry("HSZ", "hsinchu"),          // 新竹市
        Map.entry("CYI", "chiayi-city"),      // 嘉義市
        Map.entry("HSQ", "hsinchu-county"),   // 新竹縣
        Map.entry("MIA", "miaoli"),           // 苗栗縣
        Map.entry("CHA", "changhua"),         // 彰化縣
        Map.entry("NAN", "nantou"),           // 南投縣
        Map.entry("YUN", "yunlin"),           // 雲林縣
        Map.entry("CYQ", "chiayi-county"),    // 嘉義縣
        Map.entry("PIF", "pingtung"),         // 屏東縣
        Map.entry("ILA", "yilan"),            // 宜蘭縣
        Map.entry("HUA", "hualien"),          // 花蓮縣
        Map.entry("TTT", "taitung"),          // 台東縣
        Map.entry("PEN", "penghu"),           // 澎湖縣
        Map.entry("KIN", "kinmen"),           // 金門縣
        Map.entry("LIE", "lienchiang")        // 連江縣（馬祖）
    );

    /**
     * 各縣市下的城市列表
     * 注意：部分縣市可能有多個城市分頁，需要實際測試網站確認
     */
    public static final Map<String, List<String>> COUNTY_CITIES_MAP = Map.ofEntries(
        Map.entry("taipei", List.of("taipei-city")),
        Map.entry("newtaipei", List.of("newtaipei-city")),
        Map.entry("taoyuan", List.of("taoyuan-city")),
        Map.entry("taichung", List.of("taichung-city")),
        Map.entry("tainan", List.of("tainan-city")),
        Map.entry("kaohsiung", List.of("kaohsiung-city")),
        Map.entry("keelung", List.of("keelung-city")),
        Map.entry("hsinchu", List.of("hsinchu-city")),
        Map.entry("chiayi-city", List.of("chiayi-city")),
        Map.entry("hsinchu-county", List.of("hsinchu-county")),
        Map.entry("miaoli", List.of("miaoli-county")),
        Map.entry("changhua", List.of("changhua-county")),
        Map.entry("nantou", List.of("nantou-county")),
        Map.entry("yunlin", List.of("yunlin-county")),
        Map.entry("chiayi-county", List.of("chiayi-county")),
        Map.entry("pingtung", List.of("pingtung-county")),
        Map.entry("yilan", List.of("yilan-county")),
        Map.entry("hualien", List.of("hualien-county")),
        Map.entry("taitung", List.of("taitung-county")),
        Map.entry("penghu", List.of("penghu-county")),
        Map.entry("kinmen", List.of("kinmen-county")),
        Map.entry("lienchiang", List.of("lienchiang-county"))
    );

    /**
     * 根據資料庫 region code 獲取 TravelKing county slug
     */
    public String getCountySlug(String regionCode) {
        return REGION_TO_COUNTY_MAP.get(regionCode);
    }

    /**
     * 獲取縣市下的所有城市列表
     */
    public List<String> getCities(String countySlug) {
        return COUNTY_CITIES_MAP.getOrDefault(countySlug, List.of());
    }
}
