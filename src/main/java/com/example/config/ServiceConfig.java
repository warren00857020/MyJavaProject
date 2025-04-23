package com.example.config;

import com.example.repository.SightRepository;
import com.example.service.SightService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {
    @Bean
    public SightService sightService(SightRepository repository){
        System.out.println("Sight Service is created.");
        return new SightService(repository);
    }

}
