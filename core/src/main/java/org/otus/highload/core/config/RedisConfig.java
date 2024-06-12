package org.otus.highload.core.config;

import java.util.List;
import org.otus.highload.core.controller.response.PostResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

  @Bean
  public RedisTemplate<String, List<PostResponse>> redisTemplate(
      RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, List<PostResponse>> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory);
    return template;
  }
}
