package com.example.service;

import com.example.entity.Sight;
import com.example.entity.SightRequest;
import com.example.entity.SightResponse;
import com.example.parameter.SightQueryParameter;
import com.example.repository.SightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SightService 單元測試")
class SightServiceTest {

    @Mock
    private SightRepository sightRepository;

    @InjectMocks
    private SightService sightService;

    private Sight testSight;
    private SightRequest testRequest;

    @BeforeEach
    void setUp() {
        testSight = new Sight();
        testSight.setSightName("和平島公園");
        testSight.setZone("中正區");
        testSight.setCategory("風景區");
        testSight.setPhotoURL("http://example.com/photo.jpg");
        testSight.setDescription("美麗的海岸風景");
        testSight.setAddress("基隆市中正區平一路360號");
        testSight.setId(1L);

        testRequest = new SightRequest();
        testRequest.setSightName("和平島公園");
        testRequest.setZone("中正區");
        testRequest.setCategory("風景區");
        testRequest.setPhotoURL("http://example.com/photo.jpg");
        testRequest.setDescription("美麗的海岸風景");
        testRequest.setAddress("基隆市中正區平一路360號");
    }

    @Test
    @DisplayName("新增景點 - 成功")
    void createSight_Success() {
        // Given
        when(sightRepository.save(any(Sight.class))).thenReturn(testSight);

        // When
        SightResponse result = sightService.createSight(testRequest);

        // Then
        assertNotNull(result);
        assertEquals("和平島公園", result.getSightName());
        assertEquals("中正區", result.getZone());
        assertEquals("風景區", result.getCategory());
        verify(sightRepository, times(1)).save(any(Sight.class));
    }

    @Test
    @DisplayName("查詢景點 - 成功")
    void getSight_Success() {
        // Given
        when(sightRepository.findBySightName("和平島公園")).thenReturn(java.util.Optional.of(testSight));

        // When
        SightResponse result = sightService.getSight("和平島公園");

        // Then
        assertNotNull(result);
        assertEquals("和平島公園", result.getSightName());
        assertEquals("中正區", result.getZone());
        verify(sightRepository, times(1)).findBySightName("和平島公園");
    }

    @Test
    @DisplayName("刪除景點 - 成功")
    void deleteSight_Success() {
        // Given
        doNothing().when(sightRepository).deleteBySightName("和平島公園");

        // When
        sightService.deleteSight("和平島公園");

        // Then
        verify(sightRepository, times(1)).deleteBySightName("和平島公園");
    }

    


}
