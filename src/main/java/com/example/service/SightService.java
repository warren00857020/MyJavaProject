package com.example.service;

import com.example.converter.SightConverter;
import com.example.entity.Sight;
import com.example.entity.SightRequest;
import com.example.entity.SightResponse;
import com.example.parameter.SightQueryParameter;
import com.example.repository.SightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SightService {

    @Autowired
    private SightRepository repository;

    /**
     * 新增景點（使用 SightRequest DTO）
     */
    @Transactional
    public SightResponse createSight(SightRequest newSight) {
        Sight sight = SightConverter.toSight(newSight);
        Sight saved = repository.save(sight);
        return SightConverter.toSightResponse(saved);
    }

    /**
     * 直接新增景點 Entity
     */
    @Transactional
    public Sight createSightEntity(Sight sight) {
        return repository.save(sight);
    }

    /**
     * 根據景點名稱查詢（返回 Response DTO）
     */
    public SightResponse getSight(String name) {
        Optional<Sight> sight = repository.findBySightName(name);
        return sight.map(SightConverter::toSightResponse).orElse(null);
    }

    /**
     * 根據 ID 查詢景點
     */
    public Optional<Sight> getSightById(Long id) {
        return repository.findById(id);
    }

    /**
     * 根據景點名稱查詢 Entity
     */
    public Optional<Sight> getSightByName(String name) {
        return repository.findBySightName(name);
    }

    /**
     * 根據縣市 ID 查詢景點列表
     */
    public List<Sight> getSightsByRegion(Long regionId) {
        return repository.findByRegionId(regionId);
    }

    /**
     * 根據分類查詢景點列表
     */
    public List<Sight> getSightsByCategory(String category) {
        return repository.findByCategory(category);
    }

    /**
     * 複合查詢：縣市 + 分類
     */
    public List<Sight> getSightsByRegionAndCategory(Long regionId, String category) {
        return repository.findByRegionIdAndCategory(regionId, category);
    }

    /**
     * 根據行政區查詢（使用 Query Parameter）
     */
    public List<SightResponse> getSightsByZone(SightQueryParameter param) {
        String keyword = Optional.ofNullable(param.getKeyword()).orElse("");
        return repository.findByZone(keyword).stream()
                .map(SightConverter::toSightResponse)
                .toList();
    }

    /**
     * 關鍵字搜尋景點
     */
    public List<Sight> searchSights(String keyword) {
        return repository.searchByKeyword(keyword);
    }

    /**
     * 取得所有景點
     */
    public List<Sight> getAllSights() {
        return repository.findAll();
    }

    /**
     * 取得已驗證的景點
     */
    public List<Sight> getVerifiedSights() {
        return repository.findByIsVerified(true);
    }

    /**
     * 取得熱門景點
     */
    public List<Sight> getTopViewedSights() {
        return repository.findTopViewedSights();
    }

    /**
     * 更新景點資料
     */
    @Transactional
    public Sight updateSight(Long id, Sight sightDetails) {
        Sight sight = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sight not found with id: " + id));

        // 更新欄位
        sight.setSightName(sightDetails.getSightName());
        sight.setRegionId(sightDetails.getRegionId());
        sight.setZone(sightDetails.getZone());
        sight.setCategory(sightDetails.getCategory());
        sight.setPhotoURL(sightDetails.getPhotoURL());
        sight.setPhotoUrls(sightDetails.getPhotoUrls());
        sight.setDescription(sightDetails.getDescription());
        sight.setAddress(sightDetails.getAddress());
        sight.setLatitude(sightDetails.getLatitude());
        sight.setLongitude(sightDetails.getLongitude());
        sight.setOpeningHours(sightDetails.getOpeningHours());
        sight.setTicketPrice(sightDetails.getTicketPrice());
        sight.setOfficialWebsite(sightDetails.getOfficialWebsite());
        sight.setPhone(sightDetails.getPhone());
        sight.setTags(sightDetails.getTags());
        sight.setRecommendedDuration(sightDetails.getRecommendedDuration());
        sight.setSourceUrl(sightDetails.getSourceUrl());
        sight.setIsVerified(sightDetails.getIsVerified());

        return repository.save(sight);
    }

    /**
     * 刪除景點（根據名稱）
     */
    @Transactional
    public void deleteSight(String name) {
        repository.deleteBySightName(name);
    }

    /**
     * 刪除景點（根據 ID）
     */
    @Transactional
    public void deleteSightById(Long id) {
        repository.deleteById(id);
    }

    /**
     * 增加瀏覽次數
     */
    @Transactional
    public void incrementViewCount(Long id) {
        Sight sight = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sight not found with id: " + id));
        sight.setViewCount(sight.getViewCount() + 1);
        repository.save(sight);
    }
}
