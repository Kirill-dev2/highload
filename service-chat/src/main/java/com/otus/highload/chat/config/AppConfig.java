package com.otus.highload.chat.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(headerInterceptor());
  }

  @Bean
  public CommonsRequestLoggingFilter loggingFilter() {
    CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
    loggingFilter.setIncludeClientInfo(true);
    loggingFilter.setIncludeQueryString(true);
    loggingFilter.setIncludePayload(true);
    loggingFilter.setIncludeHeaders(true);
    loggingFilter.setMaxPayloadLength(64000);
    return loggingFilter;
  }

  private HandlerInterceptor headerInterceptor() {
    return new HandlerInterceptor() {
      @Override
      public void afterCompletion(
          HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        var requestId = request.getHeader("x-request-id");
        response.addHeader("x-request-id", requestId);
      }
    };
  }
}
