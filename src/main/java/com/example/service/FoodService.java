package com.example.service;

import com.example.entity.Food;
import com.example.repository.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FoodService {

    @Autowired
    private FoodRepository foodRepository;

    /**
     * 建立美食資料
     */
    @Transactional
    public Food createFood(Food food) {
        return foodRepository.save(food);
    }

    /**
     * 根據 ID 查詢美食
     */
    public Optional<Food> getFoodById(Long id) {
        return foodRepository.findById(id);
    }

    /**
     * 根據美食名稱查詢
     */
    public Optional<Food> getFoodByName(String foodName) {
        return foodRepository.findByFoodName(foodName);
    }

    /**
     * 根據縣市 ID 查詢美食列表
     */
    public List<Food> getFoodsByRegion(Long regionId) {
        return foodRepository.findByRegionId(regionId);
    }

    /**
     * 根據分類查詢美食列表
     */
    public List<Food> getFoodsByCategory(String category) {
        return foodRepository.findByCategory(category);
    }

    /**
     * 根據菜系查詢美食列表
     */
    public List<Food> getFoodsByCuisineType(String cuisineType) {
        return foodRepository.findByCuisineType(cuisineType);
    }

    /**
     * 根據價格範圍查詢美食列表
     */
    public List<Food> getFoodsByPriceRange(String priceRange) {
        return foodRepository.findByPriceRange(priceRange);
    }

    /**
     * 複合查詢：縣市 + 分類
     */
    public List<Food> getFoodsByRegionAndCategory(Long regionId, String category) {
        return foodRepository.findByRegionIdAndCategory(regionId, category);
    }

    /**
     * 關鍵字搜尋美食
     */
    public List<Food> searchFoods(String keyword) {
        return foodRepository.searchByKeyword(keyword);
    }

    /**
     * 取得所有美食
     */
    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }

    /**
     * 更新美食資料
     */
    @Transactional
    public Food updateFood(Long id, Food foodDetails) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food not found with id: " + id));

        // 更新欄位
        food.setFoodName(foodDetails.getFoodName());
        food.setRegionId(foodDetails.getRegionId());
        food.setZone(foodDetails.getZone());
        food.setCategory(foodDetails.getCategory());
        food.setCuisineType(foodDetails.getCuisineType());
        food.setPhotoUrl(foodDetails.getPhotoUrl());
        food.setPhotoUrls(foodDetails.getPhotoUrls());
        food.setDescription(foodDetails.getDescription());
        food.setAddress(foodDetails.getAddress());
        food.setLatitude(foodDetails.getLatitude());
        food.setLongitude(foodDetails.getLongitude());
        food.setOpeningHours(foodDetails.getOpeningHours());
        food.setPriceRange(foodDetails.getPriceRange());
        food.setSignatureDishes(foodDetails.getSignatureDishes());
        food.setPhone(foodDetails.getPhone());
        food.setOfficialWebsite(foodDetails.getOfficialWebsite());
        food.setTags(foodDetails.getTags());
        food.setAvgMealDuration(foodDetails.getAvgMealDuration());
        food.setSourceUrl(foodDetails.getSourceUrl());
        food.setIsVerified(foodDetails.getIsVerified());

        return foodRepository.save(food);
    }

    /**
     * 刪除美食
     */
    @Transactional
    public void deleteFood(Long id) {
        foodRepository.deleteById(id);
    }

    /**
     * 增加瀏覽次數
     */
    @Transactional
    public void incrementViewCount(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food not found with id: " + id));
        food.setViewCount(food.getViewCount() + 1);
        foodRepository.save(food);
    }
}
