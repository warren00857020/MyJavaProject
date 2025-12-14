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
        testSight = new Sight(
            "和平島公園",
            "中正區",
            "風景區",
            "http://example.com/photo.jpg",
            "美麗的海岸風景",
            "基隆市中正區平一路360號"
        );
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
        when(sightRepository.findBySightName("和平島公園")).thenReturn(testSight);

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

    @Test
    @DisplayName("依區域查詢景點 - 有結果")
    void getSightsByZone_WithResults() {
        // Given
        Sight sight2 = new Sight(
            "海洋廣場",
            "中正區",
            "廣場",
            "http://example.com/photo2.jpg",
            "港邊廣場",
            "基隆市中正區忠一路"
        );
        sight2.setId(2L);

        List<Sight> sights = Arrays.asList(testSight, sight2);
        when(sightRepository.findByZone("中正區")).thenReturn(sights);

        SightQueryParameter param = new SightQueryParameter();
        param.setKeyword("中正區");

        // When
        List<SightResponse> results = sightService.getSightsByZone(param);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("和平島公園", results.get(0).getSightName());
        assertEquals("海洋廣場", results.get(1).getSightName());
        verify(sightRepository, times(1)).findByZone("中正區");
    }

    @Test
    @DisplayName("依區域查詢景點 - 無結果")
    void getSightsByZone_NoResults() {
        // Given
        when(sightRepository.findByZone(anyString())).thenReturn(List.of());

        SightQueryParameter param = new SightQueryParameter();
        param.setKeyword("不存在的區域");

        // When
        List<SightResponse> results = sightService.getSightsByZone(param);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(sightRepository, times(1)).findByZone("不存在的區域");
    }

    @Test
    @DisplayName("依區域查詢景點 - keyword為null時使用空字串")
    void getSightsByZone_NullKeyword() {
        // Given
        when(sightRepository.findByZone("")).thenReturn(List.of());

        SightQueryParameter param = new SightQueryParameter();
        // keyword 為 null

        // When
        List<SightResponse> results = sightService.getSightsByZone(param);

        // Then
        assertNotNull(results);
        verify(sightRepository, times(1)).findByZone("");
    }
}
