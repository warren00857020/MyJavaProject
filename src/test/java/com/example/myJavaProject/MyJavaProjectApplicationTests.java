package com.example.myJavaProject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring Boot 應用程式啟動測試
 * 驗證 ApplicationContext 能正常載入
 */
@SpringBootTest
@ActiveProfiles("test")
class MyJavaProjectApplicationTests {

	// Mock crawler-related beans to avoid initialization issues
	@MockBean
	private com.example.crawler.config.SightUrlsConfig sightUrlsConfig;

	@MockBean
	private com.example.crawler.TravelKingSightCrawler travelKingSightCrawler;

	@MockBean
	private com.example.crawler.TdxScenicSpotCrawler tdxCrawler;

	@MockBean
	private com.example.crawler.CrawlerService crawlerService;

	@Test
	void contextLoads() {
		// 測試 Spring Boot ApplicationContext 能成功載入
		// 如果 context 載入失敗，此測試會拋出異常
	}

}
