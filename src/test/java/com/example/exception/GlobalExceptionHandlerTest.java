package com.example.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.service.SightService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("GlobalExceptionHandler 整合測試")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SightService sightService;

    @Test
    @DisplayName("處理 NotFoundException - 返回 404")
    void handleNotFoundException() throws Exception {
        // Given
        when(sightService.getSight(anyString()))
            .thenThrow(new NotFoundException("找不到景點"));

        // When & Then
        mockMvc.perform(get("/sights/{sightName}", "不存在的景點"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("找不到景點"))
                .andExpect(jsonPath("$.path").value("/sights/不存在的景點"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("處理 UnprocessableEntityException - 返回 422")
    void handleUnprocessableEntityException() throws Exception {
        // Given
        when(sightService.getSight(anyString()))
            .thenThrow(new UnprocessableEntityException("資料格式錯誤"));

        // When & Then
        mockMvc.perform(get("/sights/{sightName}", "test"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message").value("資料格式錯誤"));
    }

    @Test
    @DisplayName("處理 MethodArgumentNotValidException - 返回 400")
    void handleValidationException() throws Exception {
        // Given - 發送空的 sightName（違反 @NotEmpty 驗證）
        String invalidJson = """
            {
                "sightName": "",
                "zone": "中正區",
                "category": "風景區"
            }
            """;

        // When & Then
        mockMvc.perform(post("/sights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("欄位驗證失敗"))
                .andExpect(jsonPath("$.details").isArray());
    }
}
