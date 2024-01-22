package com.example.service;

import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.PostMapping;

public class MaiService {

    private String HOST = "smtp.gmail.com";
    private static final String USERNAME = "warren5606@gmail.com";
    private static final String PASSWORD = "warren0702";

    @PostConstruct
    private void init(){
        System.out.println("email_init");
    }

    public void sendMail(String username){
        System.out.println("Send email From"+USERNAME+"To"+username);
    }
}
