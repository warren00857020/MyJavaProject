package com.example.repository;

import com.example.entity.Festival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FestivalRepository extends JpaRepository<Festival, Long> {

    // 根據節慶名稱查詢
    Optional<Festival> findByFestivalName(String festivalName);

    // 根據縣市 ID 查詢
    List<Festival> findByRegionId(Long regionId);

    // 根據分類查詢（傳統節慶、音樂祭等）
    List<Festival> findByCategory(String category);

    // 根據舉辦月份查詢
    List<Festival> findByMonthHeld(Integer month);

    // 查詢特定日期區間內的節慶
    @Query("SELECT f FROM Festival f WHERE f.startDate <= :endDate AND f.endDate >= :startDate")
    List<Festival> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 查詢週期性節慶
    List<Festival> findByRecurringPattern(String recurringPattern);

    // 模糊搜尋節慶名稱
    @Query("SELECT f FROM Festival f WHERE f.festivalName LIKE %:keyword%")
    List<Festival> searchByKeyword(@Param("keyword") String keyword);
}
