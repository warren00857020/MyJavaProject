-- 初始化台灣 22 縣市資料
-- 這個檔案可以手動執行，也可以透過 Spring Boot 自動執行

INSERT INTO regions (name, code, region_type, description, created_at, updated_at) VALUES
('台北市', 'TPE', 'city', '中華民國首都，政治、經濟、文化中心', NOW(), NOW()),
('新北市', 'TPH', 'city', '環繞台北市的直轄市，人口最多', NOW(), NOW()),
('桃園市', 'TAO', 'city', '國際機場所在地，工業重鎮', NOW(), NOW()),
('台中市', 'TXG', 'city', '中台灣最大城市', NOW(), NOW()),
('台南市', 'TNN', 'city', '台灣古都，文化歷史重鎮', NOW(), NOW()),
('高雄市', 'KHH', 'city', '南台灣最大城市，重要港口', NOW(), NOW()),
('基隆市', 'KEL', 'city', '北台灣重要港口城市', NOW(), NOW()),
('新竹市', 'HSZ', 'city', '科技重鎮', NOW(), NOW()),
('嘉義市', 'CYI', 'city', '阿里山起點', NOW(), NOW()),
('新竹縣', 'HSQ', 'county', '客家文化重鎮', NOW(), NOW()),
('苗栗縣', 'MIA', 'county', '客家大縣', NOW(), NOW()),
('彰化縣', 'CHA', 'county', '農業大縣', NOW(), NOW()),
('南投縣', 'NAN', 'county', '台灣唯一不靠海的縣', NOW(), NOW()),
('雲林縣', 'YUN', 'county', '農業大縣', NOW(), NOW()),
('嘉義縣', 'CYQ', 'county', '阿里山所在地', NOW(), NOW()),
('屏東縣', 'PIF', 'county', '台灣最南端', NOW(), NOW()),
('宜蘭縣', 'ILA', 'county', '東北部觀光勝地', NOW(), NOW()),
('花蓮縣', 'HUA', 'county', '東部最大縣', NOW(), NOW()),
('台東縣', 'TTT', 'county', '東南部觀光勝地', NOW(), NOW()),
('澎湖縣', 'PEN', 'county', '離島，海洋觀光勝地', NOW(), NOW()),
('金門縣', 'KIN', 'county', '離島，戰地文化', NOW(), NOW()),
('連江縣', 'LIE', 'county', '馬祖，離島', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;
