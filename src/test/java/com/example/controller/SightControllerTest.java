package com.example.controller;

import com.example.entity.SightRequest;
import com.example.entity.SightResponse;
import com.example.service.SightService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SightController.class)
@DisplayName("SightController 整合測試")
class SightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SightService sightService;

    private SightRequest testRequest;
    private SightResponse testResponse;

    @BeforeEach
    void setUp() {
        testRequest = new SightRequest();
        testRequest.setSightName("和平島公園");
        testRequest.setZone("中正區");
        testRequest.setCategory("風景區");
        testRequest.setPhotoURL("http://example.com/photo.jpg");
        testRequest.setDescription("美麗的海岸風景");
        testRequest.setAddress("基隆市中正區平一路360號");

        testResponse = new SightResponse();
        testResponse.setSightName("和平島公園");
        testResponse.setZone("中正區");
        testResponse.setCategory("風景區");
        testResponse.setPhotoURL("http://example.com/photo.jpg");
        testResponse.setDescription("美麗的海岸風景");
        testResponse.setAddress("基隆市中正區平一路360號");
    }

    @Test
    @DisplayName("POST /sights - 新增景點成功")
    void createSight_Success() throws Exception {
        // Given
        when(sightService.createSight(any(SightRequest.class))).thenReturn(testResponse);

        // When & Then
        mockMvc.perform(post("/sights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.sightName").value("和平島公園"))
                .andExpect(jsonPath("$.zone").value("中正區"))
                .andExpect(jsonPath("$.category").value("風景區"));

        verify(sightService, times(1)).createSight(any(SightRequest.class));
    }

    @Test
    @DisplayName("POST /sights - 缺少必填欄位 sightName")
    void createSight_MissingSightName() throws Exception {
        // Given
        testRequest.setSightName("");

        // When & Then
        mockMvc.perform(post("/sights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());

        verify(sightService, never()).createSight(any(SightRequest.class));
    }

    @Test
    @DisplayName("POST /sights - 缺少必填欄位 zone")
    void createSight_MissingZone() throws Exception {
        // Given
        testRequest.setZone("");

        // When & Then
        mockMvc.perform(post("/sights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isBadRequest());

        verify(sightService, never()).createSight(any(SightRequest.class));
    }

    @Test
    @DisplayName("GET /sights/{sightName} - 查詢景點成功")
    void getSight_Success() throws Exception {
        // Given
        when(sightService.getSight("和平島公園")).thenReturn(testResponse);

        // When & Then
        mockMvc.perform(get("/sights/{sightName}", "和平島公園"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sightName").value("和平島公園"))
                .andExpect(jsonPath("$.zone").value("中正區"));

        verify(sightService, times(1)).getSight("和平島公園");
    }

    

    

    @Test
    @DisplayName("DELETE /sights/{sightName} - 刪除景點成功")
    void deleteSight_Success() throws Exception {
        // Given
        doNothing().when(sightService).deleteSight(anyString());

        // When & Then
        mockMvc.perform(delete("/sights/{sightName}", "和平島公園"))
                .andExpect(status().isNoContent());

        verify(sightService, times(1)).deleteSight("和平島公園");
    }
}
