package com.example.repository;

import com.example.entity.Sight;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SightRepository extends MongoRepository<Sight, String> {
    //Spring Boot 啟動時，便會悄悄地根據方法的定義實作出來。比方說 findById 方法，它的名稱會被解讀，變成真的能查詢資料的方法。
    Sight findBySightName(String name);
    void deleteBySightName(String name);
    List<Sight> findByZone(String zone);
}
