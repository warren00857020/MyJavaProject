package com.example;

import com.example.entity.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.util.List;

@SpringBootApplication
@EnableScheduling  // 啟用 Spring Scheduling 功能
public class
MyJavaProjectApplication {

    public MyJavaProjectApplication() {

    }

    public static void main(String[] args) {
		SpringApplication.run(MyJavaProjectApplication.class, args);
	}
}
