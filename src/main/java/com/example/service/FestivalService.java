package com.example.service;

import com.example.entity.Festival;
import com.example.repository.FestivalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class FestivalService {

    @Autowired
    private FestivalRepository festivalRepository;

    /**
     * 建立節慶資料
     */
    @Transactional
    public Festival createFestival(Festival festival) {
        return festivalRepository.save(festival);
    }

    /**
     * 根據 ID 查詢節慶
     */
    public Optional<Festival> getFestivalById(Long id) {
        return festivalRepository.findById(id);
    }

    /**
     * 根據節慶名稱查詢
     */
    public Optional<Festival> getFestivalByName(String festivalName) {
        return festivalRepository.findByFestivalName(festivalName);
    }

    /**
     * 根據縣市 ID 查詢節慶列表
     */
    public List<Festival> getFestivalsByRegion(Long regionId) {
        return festivalRepository.findByRegionId(regionId);
    }

    /**
     * 根據分類查詢節慶列表
     */
    public List<Festival> getFestivalsByCategory(String category) {
        return festivalRepository.findByCategory(category);
    }

    /**
     * 根據舉辦月份查詢節慶列表
     */
    public List<Festival> getFestivalsByMonth(Integer month) {
        return festivalRepository.findByMonthHeld(month);
    }

    /**
     * 查詢特定日期區間內的節慶
     */
    public List<Festival> getFestivalsByDateRange(LocalDate startDate, LocalDate endDate) {
        return festivalRepository.findByDateRange(startDate, endDate);
    }

    /**
     * 查詢週期性節慶
     */
    public List<Festival> getFestivalsByRecurringPattern(String recurringPattern) {
        return festivalRepository.findByRecurringPattern(recurringPattern);
    }

    /**
     * 關鍵字搜尋節慶
     */
    public List<Festival> searchFestivals(String keyword) {
        return festivalRepository.searchByKeyword(keyword);
    }

    /**
     * 取得所有節慶
     */
    public List<Festival> getAllFestivals() {
        return festivalRepository.findAll();
    }

    /**
     * 更新節慶資料
     */
    @Transactional
    public Festival updateFestival(Long id, Festival festivalDetails) {
        Festival festival = festivalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Festival not found with id: " + id));

        // 更新欄位
        festival.setFestivalName(festivalDetails.getFestivalName());
        festival.setRegionId(festivalDetails.getRegionId());
        festival.setZone(festivalDetails.getZone());
        festival.setCategory(festivalDetails.getCategory());
        festival.setPhotoUrl(festivalDetails.getPhotoUrl());
        festival.setPhotoUrls(festivalDetails.getPhotoUrls());
        festival.setDescription(festivalDetails.getDescription());
        festival.setLocation(festivalDetails.getLocation());
        festival.setAddress(festivalDetails.getAddress());
        festival.setLatitude(festivalDetails.getLatitude());
        festival.setLongitude(festivalDetails.getLongitude());
        festival.setStartDate(festivalDetails.getStartDate());
        festival.setEndDate(festivalDetails.getEndDate());
        festival.setRecurringPattern(festivalDetails.getRecurringPattern());
        festival.setMonthHeld(festivalDetails.getMonthHeld());
        festival.setIsLunar(festivalDetails.getIsLunar());
        festival.setEventSchedule(festivalDetails.getEventSchedule());
        festival.setOfficialWebsite(festivalDetails.getOfficialWebsite());
        festival.setContactInfo(festivalDetails.getContactInfo());
        festival.setTags(festivalDetails.getTags());
        festival.setExpectedDuration(festivalDetails.getExpectedDuration());
        festival.setTicketRequired(festivalDetails.getTicketRequired());
        festival.setTicketInfo(festivalDetails.getTicketInfo());
        festival.setSourceUrl(festivalDetails.getSourceUrl());
        festival.setIsVerified(festivalDetails.getIsVerified());

        return festivalRepository.save(festival);
    }

    /**
     * 刪除節慶
     */
    @Transactional
    public void deleteFestival(Long id) {
        festivalRepository.deleteById(id);
    }

    /**
     * 增加瀏覽次數
     */
    @Transactional
    public void incrementViewCount(Long id) {
        Festival festival = festivalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Festival not found with id: " + id));
        festival.setViewCount(festival.getViewCount() + 1);
        festivalRepository.save(festival);
    }
}
