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
    @Autowired
    private SightRepository repository;

    //新增景點
    public SightResponse createSight(SightRequest newSight) {
        Sight sight = SightConverter.toSight(newSight);
        return SightConverter.toSightResponse(repository.insert(sight));
    }

    public SightResponse getSight(String name) {
        return SightConverter.toSightResponse(repository.findBySightName(name));
    }

    public void deleteSight(String name) {
        SightResponse sight = getSight(name);
        repository.deleteBySightName(sight.getSightName());
    }

    public List<SightResponse> getSightsByZone(SightQueryParameter param){
        String keyword = Optional.ofNullable(param.getKeyword()).orElse("");
        List<Sight> sights= repository.findByZone(keyword);
        List<SightResponse> sightResponses = new ArrayList<>();
        for (Sight s: sights) {
            sightResponses.add(SightConverter.toSightResponse(s));
        }
        return sightResponses;
    }

}
