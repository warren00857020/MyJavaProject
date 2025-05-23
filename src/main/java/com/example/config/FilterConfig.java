package com.example.config;

import com.example.filter.LogApiFilter;
import com.example.filter.LogProcessTimeFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {



    @Bean
    public FilterRegistrationBean logProcessTimeFilter(){
        FilterRegistrationBean<LogProcessTimeFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new LogProcessTimeFilter());
        bean.addUrlPatterns("/*");
        bean.setName("logProcessTimeFilter");
        bean.setOrder(1);

        return bean;
    }

}
