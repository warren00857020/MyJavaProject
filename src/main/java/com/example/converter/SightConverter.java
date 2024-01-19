package com.example.converter;

import com.example.entity.Sight;
import com.example.entity.SightRequest;
import com.example.entity.SightResponse;

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

    public static SightResponse toSightResponse(Sight sight){
        SightResponse sightResponse = new SightResponse();
        sightResponse.setSightName(sight.getSightName());
        sightResponse.setZone(sight.getZone());
        sightResponse.setCategory(sight.getCategory());
        sightResponse.setPhotoURL(sight.getPhotoURL());
        sightResponse.setDescription(sight.getDescription());
        sightResponse.setAddress(sight.getAddress());

        return sightResponse;

    }
}
