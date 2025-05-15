package com.example.controller;

import com.example.crawler.KeelungSightsCrawler;
import com.example.entity.*;
import com.example.parameter.SightQueryParameter;
import com.example.service.SightService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;


@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class SightController {
    @Autowired
    private SightService sightService;

    //爬蟲進DB
    @GetMapping("/setSights")//
    public ResponseEntity<List<SightRequest>> putSights(@RequestParam String zone) throws IOException {
        KeelungSightsCrawler crawler = new KeelungSightsCrawler();
        List<SightRequest> sights = crawler.getItems(zone);
        for (SightRequest s: sights) {
            sightService.createSight(s);
        }
        return ResponseEntity.ok().body(sights);
    }

    //新增景點(#用postman newSight沒抓到)
    @PostMapping("/sights")
    public ResponseEntity<SightResponse> createSight(@Valid @RequestBody SightRequest newSight){

        SightResponse sight= sightService.createSight(newSight);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{sightName}").buildAndExpand(sight.getSightName()).toUri();
        //這段程式碼的目的是建立一個URI，用來指向新創建的產品
        return ResponseEntity.created(location).body(sight);
    }

    //查詢景點
    @GetMapping("/sights/{sightName}")
    public ResponseEntity<SightResponse> getSight(@PathVariable("sightName") String name){
        SightResponse sight = sightService.getSight(name);

        return ResponseEntity.ok(sight);
    }

    //查詢區域所有景點
    @GetMapping("/sights") // /sights?keyword=七堵區
    public ResponseEntity<List<SightResponse>> getSights(@ModelAttribute SightQueryParameter param){
        List<SightResponse> sights = sightService.getSightsByZone(param);
        return ResponseEntity.ok(sights);
    }

    //刪除景點
    @DeleteMapping("/sights/{sightName}")
    public ResponseEntity<Void> deleteSight(@PathVariable("sightName") String name) {
        sightService.deleteSight(name);

        return ResponseEntity.noContent().build();
    }


}
