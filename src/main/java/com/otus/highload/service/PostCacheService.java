package com.otus.highload.service;

import com.otus.highload.controller.response.PostResponse;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostCacheService {
  private final RedisTemplate<String, List<PostResponse>> redisTemplate;

  public List<PostResponse> getPosts(String userId) {
    log.debug("read from cache");
    var postResponses = redisTemplate.opsForValue().get(userId);
    if (postResponses != null) {
      log.debug("read {} objects", postResponses.size());
    }
    return postResponses;
  }

  @Async
  public void addToCache(String userId, List<PostResponse> postResponses) {
    log.debug("write to cache");
    redisTemplate.opsForValue().set(userId, postResponses, Duration.ofMinutes(1));
  }

  public void invalidate(String userId) {
    var delete = redisTemplate.delete(userId);
    log.debug("result invalidate {}", delete);
  }
}
