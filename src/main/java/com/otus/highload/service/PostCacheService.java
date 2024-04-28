package com.otus.highload.service;

import com.otus.highload.controller.response.PostResponse;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostCacheService {
  private final FriendService friendService;
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
    var friends = friendService.findAllFriendBy(userId);
    var stream = friends.size() > 500 ? friends.parallelStream() : friends.stream();
    stream
        .flatMap(f -> Stream.of(f.getUserId(), f.getFriendId()))
        .collect(Collectors.toSet())
        .forEach(
            id -> {
              var delete = redisTemplate.delete(id);
              log.debug("result invalidate {}", delete);
            });
  }
}
