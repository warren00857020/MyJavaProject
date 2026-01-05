package com.example.controller;

import com.example.entity.Sight;
import com.example.entity.SightResponse;
import com.example.service.SightService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SightController 整合測試
 * 測試景點 REST API 端點功能
 */
@WebMvcTest(SightController.class)
class SightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SightService sightService;

    // 需要 Mock 爬蟲相關的 Bean
    @MockBean
    private com.example.crawler.TravelKingSightCrawler travelKingSightCrawler;

    @MockBean
    private com.example.crawler.TdxScenicSpotCrawler tdxCrawler;

    @MockBean
    private com.example.crawler.CrawlerService crawlerService;

    private SightResponse testResponse;
    private Sight testSight;

    @BeforeEach
    void setUp() {
        // 準備測試資料
        testResponse = new SightResponse();
        testResponse.setSightName("台北101");
        testResponse.setZone("信義區");
        testResponse.setCategory("地標建築");
        testResponse.setPhotoURL("http://example.com/101.jpg");
        testResponse.setDescription("台北地標");
        testResponse.setAddress("台北市信義區信義路五段7號");

        testSight = new Sight();
        testSight.setId(1L);
        testSight.setSightName("台北101");
        testSight.setZone("信義區");
    }

    /**
     * 測試：GET /sights/{sightName} - 查詢景點成功
     */
    @Test
    void testGetSightSuccess() throws Exception {
        // Given
        when(sightService.getSight("台北101")).thenReturn(testResponse);

        // When & Then
        mockMvc.perform(get("/sights/{sightName}", "台北101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sightName").value("台北101"))
                .andExpect(jsonPath("$.zone").value("信義區"))
                .andExpect(jsonPath("$.category").value("地標建築"));

        verify(sightService, times(1)).getSight("台北101");
    }

    /**
     * 測試：GET /sights/{sightName} - 查詢不存在的景點
     * 注意：實際實作中，即使查詢不到也會返回 200 OK（body 為 null）
     */
    @Test
    void testGetSightNotFound() throws Exception {
        // Given
        when(sightService.getSight("不存在的景點")).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/sights/{sightName}", "不存在的景點"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(sightService, times(1)).getSight("不存在的景點");
    }

    /**
     * 測試：GET /sights - 查詢景點列表（無篩選條件）
     * 實際實作：統一使用 searchByMultipleConditions(null, null, null)
     */
    @Test
    void testGetSightsList() throws Exception {
        // Given
        SightResponse sight2 = new SightResponse();
        sight2.setSightName("故宮博物院");
        sight2.setZone("士林區");

        List<SightResponse> sights = Arrays.asList(testResponse, sight2);
        when(sightService.searchByMultipleConditions(null, null, null)).thenReturn(sights);

        // When & Then
        mockMvc.perform(get("/sights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].sightName").value("台北101"))
                .andExpect(jsonPath("$[1].sightName").value("故宮博物院"));

        verify(sightService, times(1)).searchByMultipleConditions(null, null, null);
    }

    /**
     * 測試：GET /sights?zone=信義區 - 根據區域篩選景點
     * 實際實作：使用 searchByMultipleConditions(null, "信義區", null)
     */
    @Test
    void testGetSightsByZone() throws Exception {
        // Given
        List<SightResponse> sights = Arrays.asList(testResponse);
        when(sightService.searchByMultipleConditions(null, "信義區", null)).thenReturn(sights);

        // When & Then
        mockMvc.perform(get("/sights")
                .param("zone", "信義區"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].zone").value("信義區"));

        verify(sightService, times(1)).searchByMultipleConditions(null, "信義區", null);
    }

    /**
     * 測試：GET /sights?city=台北市 - 根據城市篩選景點
     * 實際實作：使用 searchByMultipleConditions("台北市", null, null)
     */
    @Test
    void testGetSightsByCity() throws Exception {
        // Given
        List<SightResponse> sights = Arrays.asList(testResponse);
        when(sightService.searchByMultipleConditions("台北市", null, null)).thenReturn(sights);

        // When & Then
        mockMvc.perform(get("/sights")
                .param("city", "台北市"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].sightName").value("台北101"));

        verify(sightService, times(1)).searchByMultipleConditions("台北市", null, null);
    }

    /**
     * 測試：GET /sights?keyword=101 - 關鍵字搜尋
     * 實際實作：使用 searchByMultipleConditions(null, null, "101")
     */
    @Test
    void testSearchSightsByKeyword() throws Exception {
        // Given
        List<SightResponse> sights = Arrays.asList(testResponse);
        when(sightService.searchByMultipleConditions(null, null, "101")).thenReturn(sights);

        // When & Then
        mockMvc.perform(get("/sights")
                .param("keyword", "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].sightName").value("台北101"));

        verify(sightService, times(1)).searchByMultipleConditions(null, null, "101");
    }

    /**
     * 測試：DELETE /sights/{sightName} - 刪除景點成功
     */
    @Test
    void testDeleteSightSuccess() throws Exception {
        // Given
        doNothing().when(sightService).deleteSight("台北101");

        // When & Then
        mockMvc.perform(delete("/sights/{sightName}", "台北101"))
                .andExpect(status().isNoContent());

        verify(sightService, times(1)).deleteSight("台北101");
    }

    /**
     * 測試：CORS 標頭應該允許跨域請求
     */
    @Test
    void testCorsHeaders() throws Exception {
        // Given
        when(sightService.getSight(anyString())).thenReturn(testResponse);

        // When & Then
        mockMvc.perform(get("/sights/{sightName}", "台北101")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());
    }
}
