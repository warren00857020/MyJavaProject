package com.example.service;

import com.example.converter.SightConverter;
import com.example.entity.Sight;
import com.example.entity.SightRequest;
import com.example.entity.SightResponse;
import com.example.parameter.SightQueryParameter;
import com.example.repository.SightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SightService {
    private final SightRepository repository;

    @Autowired
    public SightService(SightRepository repository) {
        this.repository = repository;
    }

    /** 新增景點 */
    public SightResponse createSight(SightRequest newSight) {
        Sight sight = SightConverter.toSight(newSight);
        Sight saved = repository.save(sight);          // ← save 取代 insert
        return SightConverter.toSightResponse(saved);
    }

    public SightResponse getSight(String name) {
        return SightConverter.toSightResponse(
                repository.findBySightName(name));
    }

    public void deleteSight(String name) {
        repository.deleteBySightName(name);            // 直接呼叫 repo
    }

    public List<SightResponse> getSightsByZone(SightQueryParameter param) {
        String keyword = Optional.ofNullable(param.getKeyword()).orElse("");
        return repository.findByZone(keyword).stream()
                .map(SightConverter::toSightResponse)
                .toList();
    }

}
