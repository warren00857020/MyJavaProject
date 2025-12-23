package com.example.repository;

import com.example.entity.Sight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SightRepository extends JpaRepository<Sight, Long> {

    // 根據景點名稱查詢
    Optional<Sight> findBySightName(String name);

    // 刪除景點（根據名稱）
    void deleteBySightName(String name);

    // 根據行政區查詢
    List<Sight> findByZone(String zone);

    // 根據縣市 ID 查詢
    List<Sight> findByRegionId(Long regionId);

    // 根據分類查詢（古蹟、博物館等）
    List<Sight> findByCategory(String category);

    // 複合查詢：縣市 + 分類
    List<Sight> findByRegionIdAndCategory(Long regionId, String category);

    // 模糊搜尋景點名稱
    @Query("SELECT s FROM Sight s WHERE s.sightName LIKE %:keyword%")
    List<Sight> searchByKeyword(@Param("keyword") String keyword);

    // 根據是否驗證查詢
    List<Sight> findByIsVerified(Boolean isVerified);

    // 查詢熱門景點（根據瀏覽次數排序）
    @Query("SELECT s FROM Sight s ORDER BY s.viewCount DESC")
    List<Sight> findTopViewedSights();
}
