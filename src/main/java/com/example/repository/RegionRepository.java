package com.example.repository;

import com.example.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

    // 根據縣市名稱查詢
    Optional<Region> findByName(String name);

    // 根據縣市代碼查詢
    Optional<Region> findByCode(String code);

    // 根據類型查詢（city 或 county）
    Iterable<Region> findByRegionType(String regionType);
}
