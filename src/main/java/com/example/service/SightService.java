package com.example.service;

import com.example.converter.SightConverter;
import com.example.entity.Sight;
import com.example.entity.SightRequest;
import com.example.parameter.SightQueryParameter;
import com.example.repository.SightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SightService {
    @Autowired
    private SightRepository repository;

    //新增景點
    public Sight createSight(SightRequest newSight) {

        Sight sight = SightConverter.toSight(newSight);
        //System.out.println(sight);
        return repository.insert(sight);
    }

    public Sight getSight(String name) {
        return repository.findBySightName(name);
    }

    public void deleteSight(String name) {
        Sight sight = getSight(name);
        repository.deleteBySightName(sight.getSightName());
    }

    public List<Sight> getSightsByZone(SightQueryParameter param){
        String keyword = Optional.ofNullable(param.getKeyword()).orElse("");

        return repository.findByZone(keyword);
    }

}
