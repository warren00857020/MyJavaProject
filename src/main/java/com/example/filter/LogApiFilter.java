package com.example.filter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//它能打印出被呼叫的 API 及其回應的狀態碼
public class LogApiFilter  extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, jakarta.servlet.FilterChain chain) throws ServletException, IOException {
        int httpStatus = response.getStatus();
        String httpMethod = request.getMethod();
        String uri = request.getRequestURI();
        String params = request.getQueryString();

        if(params!=null){
            uri += "?" + params;
        }

        System.out.println(String.join("",String.valueOf(httpStatus),httpMethod,uri));
    }
}
