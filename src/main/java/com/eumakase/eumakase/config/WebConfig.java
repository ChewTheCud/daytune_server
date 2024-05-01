package com.eumakase.eumakase.config;

import com.eumakase.eumakase.security.HttpMethodFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public FilterRegistrationBean<HttpMethodFilter> httpMethodFilter(){
        FilterRegistrationBean<HttpMethodFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new HttpMethodFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
