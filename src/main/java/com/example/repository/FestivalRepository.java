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

    // 根據來源 URL 查詢（用於去重）
    Optional<Festival> findBySourceUrl(String sourceUrl);

    // 根據活動 ID 查詢
    Optional<Festival> findByEventId(String eventId);

    // 根據節慶名稱查詢
    Optional<Festival> findByFestivalName(String festivalName);

    // 根據區域查詢
    List<Festival> findByZone(String zone);

    // 根據縣市 ID 查詢
    List<Festival> findByRegionId(Long regionId);

    // 根據分類查詢（傳統節慶、音樂祭等）
    List<Festival> findByCategory(String category);

    // 根據舉辦月份查詢
    List<Festival> findByMonthHeld(Integer month);

    // 查詢特定日期區間內的節慶
    @Query("SELECT f FROM Festival f WHERE f.startDate <= :endDate AND f.endDate >= :startDate")
    List<Festival> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 查詢進行中的活動（當前日期在開始和結束之間）
    @Query("SELECT f FROM Festival f WHERE f.startDate <= :date AND f.endDate >= :date")
    List<Festival> findOngoingFestivals(@Param("date") LocalDate date);

    // 查詢即將開始的活動（未來 N 天內）
    @Query("SELECT f FROM Festival f WHERE f.startDate > :now AND f.startDate <= :futureDate ORDER BY f.startDate ASC")
    List<Festival> findUpcomingFestivals(@Param("now") LocalDate now, @Param("futureDate") LocalDate futureDate);

    // 查詢週期性節慶
    List<Festival> findByRecurringPattern(String recurringPattern);

    // 模糊搜尋節慶名稱
    @Query("SELECT f FROM Festival f WHERE f.festivalName LIKE %:keyword%")
    List<Festival> searchByKeyword(@Param("keyword") String keyword);
}
