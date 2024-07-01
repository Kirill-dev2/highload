package com.otus.highload.core.feign;

import feign.RequestInterceptor;
import java.util.UUID;
import org.springframework.context.annotation.Bean;

public class HeaderConfig {
  @Bean
  public RequestInterceptor headerInterceptor() {
    return requestTemplate -> requestTemplate.header("x-request-id", UUID.randomUUID().toString());
  }
}
