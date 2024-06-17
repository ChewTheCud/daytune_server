package com.eumakase.eumakase.config;

import com.eumakase.eumakase.security.HttpMethodFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    @Bean
    public FilterRegistrationBean<HttpMethodFilter> httpMethodFilter(){
        FilterRegistrationBean<HttpMethodFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new HttpMethodFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
