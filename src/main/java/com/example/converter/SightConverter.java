package com.example.converter;

import com.example.entity.Sight;
import com.example.entity.SightRequest;

public class SightConverter {
    private SightConverter(){

    }

    public static Sight toSight(SightRequest sightRequest){
        Sight sight = new Sight();
        sight.setSightName(sightRequest.getSightName());
        sight.setZone(sightRequest.getZone());
        sight.setCategory(sightRequest.getCategory());
        sight.setPhotoURL(sightRequest.getPhotoURL());
        sight.setDescription(sightRequest.getDescription());
        sight.setAddress(sightRequest.getAddress());

        return sight;
    }
}
