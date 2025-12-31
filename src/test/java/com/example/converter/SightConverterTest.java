package com.example.converter;

import com.example.entity.Sight;
import com.example.entity.SightRequest;
import com.example.entity.SightResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SightConverter 單元測試")
class SightConverterTest {

    @Test
    @DisplayName("SightRequest 轉換為 Sight")
    void toSight_Success() {
        // Given
        SightRequest request = new SightRequest();
        request.setSightName("和平島公園");
        request.setZone("中正區");
        request.setCategory("風景區");
        request.setPhotoURL("http://example.com/photo.jpg");
        request.setDescription("美麗的海岸風景");
        request.setAddress("基隆市中正區平一路360號");

        // When
        Sight sight = SightConverter.toSight(request);

        // Then
        assertNotNull(sight);
        assertEquals("和平島公園", sight.getSightName());
        assertEquals("中正區", sight.getZone());
        assertEquals("風景區", sight.getCategory());
        assertEquals("http://example.com/photo.jpg", sight.getPhotoURL());
        assertEquals("美麗的海岸風景", sight.getDescription());
        assertEquals("基隆市中正區平一路360號", sight.getAddress());
    }

    @Test
    @DisplayName("Sight 轉換為 SightResponse")
    void toSightResponse_Success() {
        // Given
        Sight sight = new Sight();
        sight.setSightName("和平島公園");
        sight.setZone("中正區");
        sight.setCategory("風景區");
        sight.setPhotoURL("http://example.com/photo.jpg");
        sight.setDescription("美麗的海岸風景");
        sight.setAddress("基隆市中正區平一路360號");
        sight.setId(1L);

        // When
        SightResponse response = SightConverter.toSightResponse(sight);

        // Then
        assertNotNull(response);
        assertEquals("和平島公園", response.getSightName());
        assertEquals("中正區", response.getZone());
        assertEquals("風景區", response.getCategory());
        assertEquals("http://example.com/photo.jpg", response.getPhotoURL());
        assertEquals("美麗的海岸風景", response.getDescription());
        assertEquals("基隆市中正區平一路360號", response.getAddress());
    }

    @Test
    @DisplayName("轉換空值資料")
    void toSight_WithEmptyValues() {
        // Given
        SightRequest request = new SightRequest();
        request.setSightName("");
        request.setZone("");
        request.setCategory("");

        // When
        Sight sight = SightConverter.toSight(request);

        // Then
        assertNotNull(sight);
        assertEquals("", sight.getSightName());
        assertEquals("", sight.getZone());
        assertEquals("", sight.getCategory());
    }
}
