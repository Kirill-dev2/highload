package com.otus.highload.core.controller;

import com.otus.highload.core.security.AuthenticationUtil;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.otus.highload.core.controller.request.CreatePostRequest;
import com.otus.highload.core.controller.request.UpdatePostRequest;
import com.otus.highload.core.controller.response.PostResponse;
import com.otus.highload.core.dao.Post;
import com.otus.highload.core.service.PostCacheService;
import com.otus.highload.core.service.PostService;
import com.otus.highload.core.service.StompService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {
  private final PostCacheService cacheService;
  private final PostService postService;
  private final StompService stompService;

  @PostMapping("/create")
  public Map<String, String> create(@Validated @RequestBody CreatePostRequest request) {
    var userId = AuthenticationUtil.extractUserId();
    var post = postService.create(userId, request.text());
    cacheService.invalidate(userId);
    stompService.sendToFriends(userId, buildResponse(post));
    return Map.of("post_id", post.getId());
  }

  @PutMapping("/update")
  public ResponseEntity<String> update(@Validated @RequestBody UpdatePostRequest request) {
    var userId = AuthenticationUtil.extractUserId();
    postService.updateBy(userId, request);
    return ResponseEntity.ok("Успешно изменен пост");
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<String> delete(@PathVariable(name = "id") String id) {
    postService.deleteBy(id);
    return ResponseEntity.ok("Успешно удален пост");
  }

  @GetMapping("/get/{id}")
  public PostResponse get(@PathVariable(name = "id") String id) {
    var userId = AuthenticationUtil.extractUserId();
    var post = postService.findBy(userId, id);
    return buildResponse(post);
  }

  @GetMapping("/feed")
  public List<PostResponse> feed(
      @RequestParam("offset") Long offset, @Positive @RequestParam("limit") Long limit) {
    var userId = AuthenticationUtil.extractUserId();
    var postResponses = cacheService.getPosts(userId);
    if (postResponses != null && !postResponses.isEmpty()) {
      return postResponses;
    } else {
      var posts = postService.findFirstAt(userId, Long.max(offset, 0), limit);
      var stream = posts.size() > 500 ? posts.parallelStream() : posts.stream();
      var responses = stream.map(this::buildResponse).toList();
      cacheService.addToCache(userId, responses);
      return responses;
    }
  }

  private PostResponse buildResponse(Post post) {
    return new PostResponse(post.getId(), post.getText(), post.getUserId());
  }
}