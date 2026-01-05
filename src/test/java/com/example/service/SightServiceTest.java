package com.example.service;

import com.example.entity.Sight;
import com.example.entity.SightRequest;
import com.example.entity.SightResponse;
import com.example.repository.SightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SightService 單元測試
 * 測試景點服務的業務邏輯
 */
@ExtendWith(MockitoExtension.class)
class SightServiceTest {

    @Mock
    private SightRepository repository;

    @InjectMocks
    private SightService sightService;

    private Sight testSight;
    private SightRequest testRequest;

    @BeforeEach
    void setUp() {
        // 準備測試資料
        testSight = new Sight();
        testSight.setId(1L);
        testSight.setSightName("台北101");
        testSight.setZone("信義區");
        testSight.setCategory("地標建築");
        testSight.setPhotoURL("http://example.com/101.jpg");
        testSight.setDescription("台北地標");
        testSight.setAddress("台北市信義區信義路五段7號");

        testRequest = new SightRequest();
        testRequest.setSightName("台北101");
        testRequest.setZone("信義區");
        testRequest.setCategory("地標建築");
    }

    /**
     * 測試：成功新增景點（使用 Request DTO）
     */
    @Test
    void testCreateSight() {
        // Given
        when(repository.save(any(Sight.class))).thenReturn(testSight);

        // When
        SightResponse response = sightService.createSight(testRequest);

        // Then
        assertNotNull(response);
        assertEquals("台北101", response.getSightName());
        assertEquals("信義區", response.getZone());
        verify(repository, times(1)).save(any(Sight.class));
    }

    /**
     * 測試：直接新增景點 Entity
     */
    @Test
    void testCreateSightEntity() {
        // Given
        when(repository.save(testSight)).thenReturn(testSight);

        // When
        Sight saved = sightService.createSightEntity(testSight);

        // Then
        assertNotNull(saved);
        assertEquals("台北101", saved.getSightName());
        verify(repository, times(1)).save(testSight);
    }

    /**
     * 測試：根據名稱查詢景點（找到）
     */
    @Test
    void testGetSightByNameFound() {
        // Given
        when(repository.findBySightName("台北101")).thenReturn(Optional.of(testSight));

        // When
        SightResponse response = sightService.getSight("台北101");

        // Then
        assertNotNull(response);
        assertEquals("台北101", response.getSightName());
        assertEquals("信義區", response.getZone());
        verify(repository, times(1)).findBySightName("台北101");
    }

    /**
     * 測試：根據名稱查詢景點（找不到）
     */
    @Test
    void testGetSightByNameNotFound() {
        // Given
        when(repository.findBySightName("不存在的景點")).thenReturn(Optional.empty());

        // When
        SightResponse response = sightService.getSight("不存在的景點");

        // Then
        assertNull(response);
        verify(repository, times(1)).findBySightName("不存在的景點");
    }

    /**
     * 測試：根據 ID 查詢景點
     */
    @Test
    void testGetSightById() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(testSight));

        // When
        Optional<Sight> result = sightService.getSightById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("台北101", result.get().getSightName());
        verify(repository, times(1)).findById(1L);
    }

    /**
     * 測試：根據區域查詢景點列表
     */
    @Test
    void testGetSightsByZoneName() {
        // Given
        Sight sight2 = new Sight();
        sight2.setSightName("象山步道");
        sight2.setZone("信義區");

        List<Sight> sights = Arrays.asList(testSight, sight2);
        when(repository.findByZone("信義區")).thenReturn(sights);

        // When
        List<SightResponse> results = sightService.getSightsByZoneName("信義區");

        // Then
        assertEquals(2, results.size());
        assertEquals("台北101", results.get(0).getSightName());
        assertEquals("象山步道", results.get(1).getSightName());
        verify(repository, times(1)).findByZone("信義區");
    }

    /**
     * 測試：根據分類查詢景點列表
     */
    @Test
    void testGetSightsByCategory() {
        // Given
        List<Sight> sights = Arrays.asList(testSight);
        when(repository.findByCategory("地標建築")).thenReturn(sights);

        // When
        List<Sight> results = sightService.getSightsByCategory("地標建築");

        // Then
        assertEquals(1, results.size());
        assertEquals("台北101", results.get(0).getSightName());
        verify(repository, times(1)).findByCategory("地標建築");
    }

    /**
     * 測試：關鍵字搜尋景點
     */
    @Test
    void testSearchSights() {
        // Given
        List<Sight> sights = Arrays.asList(testSight);
        when(repository.searchByKeyword("101")).thenReturn(sights);

        // When
        List<Sight> results = sightService.searchSights("101");

        // Then
        assertEquals(1, results.size());
        assertEquals("台北101", results.get(0).getSightName());
        verify(repository, times(1)).searchByKeyword("101");
    }

    /**
     * 測試：查詢所有景點
     */
    @Test
    void testGetAllSights() {
        // Given
        Sight sight2 = new Sight();
        sight2.setSightName("故宮博物院");

        List<Sight> sights = Arrays.asList(testSight, sight2);
        when(repository.findAll()).thenReturn(sights);

        // When
        List<Sight> results = sightService.getAllSights();

        // Then
        assertEquals(2, results.size());
        verify(repository, times(1)).findAll();
    }

    /**
     * 測試：根據名稱刪除景點
     */
    @Test
    void testDeleteSightByName() {
        // Given
        doNothing().when(repository).deleteBySightName("台北101");

        // When
        sightService.deleteSight("台北101");

        // Then
        verify(repository, times(1)).deleteBySightName("台北101");
    }

    /**
     * 測試：根據 ID 刪除景點
     */
    @Test
    void testDeleteSightById() {
        // Given
        doNothing().when(repository).deleteById(1L);

        // When
        sightService.deleteSightById(1L);

        // Then
        verify(repository, times(1)).deleteById(1L);
    }

    /**
     * 測試：更新景點資訊
     */
    @Test
    void testUpdateSight() {
        // Given
        Sight updatedSight = new Sight();
        updatedSight.setSightName("台北101觀景台");
        updatedSight.setDescription("更新後的描述");

        when(repository.findById(1L)).thenReturn(Optional.of(testSight));
        when(repository.save(any(Sight.class))).thenReturn(testSight);

        // When
        Sight result = sightService.updateSight(1L, updatedSight);

        // Then
        assertNotNull(result);
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(Sight.class));
    }
}
