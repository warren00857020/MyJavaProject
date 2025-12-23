package com.example.repository;

import com.example.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    // 根據美食名稱查詢
    Optional<Food> findByFoodName(String foodName);

    // 根據縣市 ID 查詢
    List<Food> findByRegionId(Long regionId);

    // 根據行政區查詢
    List<Food> findByZone(String zone);

    // 根據分類查詢（小吃、餐廳、甜點等）
    List<Food> findByCategory(String category);

    // 根據菜系查詢（台菜、客家菜等）
    List<Food> findByCuisineType(String cuisineType);

    // 根據價格範圍查詢
    List<Food> findByPriceRange(String priceRange);

    // 複合查詢：縣市 + 分類
    List<Food> findByRegionIdAndCategory(Long regionId, String category);

    // 模糊搜尋美食名稱
    @Query("SELECT f FROM Food f WHERE f.foodName LIKE %:keyword%")
    List<Food> searchByKeyword(@Param("keyword") String keyword);
}
