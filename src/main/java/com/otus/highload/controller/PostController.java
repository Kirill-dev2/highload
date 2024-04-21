package com.otus.highload.controller;

import com.otus.highload.controller.request.CreatePostRequest;
import com.otus.highload.controller.request.UpdatePostRequest;
import com.otus.highload.controller.response.PostResponse;
import com.otus.highload.dao.Post;
import com.otus.highload.security.AuthenticationUtil;
import com.otus.highload.service.PostCacheService;
import com.otus.highload.service.PostService;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @PostMapping("/create")
  public Map<String, String> create(@Validated @RequestBody CreatePostRequest request) {
    var userId = AuthenticationUtil.extractUserId();
    var post = postService.create(userId, request);
    cacheService.invalidate(userId);
    return Map.of("post_id", post.getId());
  }

  @PutMapping("/update")
  public ResponseEntity<String> update(@Validated @RequestBody UpdatePostRequest request) {
    postService.updateBy(request);
    return ResponseEntity.ok("Успешно изменен пост");
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<String> delete(@PathVariable(name = "id") String id) {
    postService.deleteBy(id);
    return ResponseEntity.ok("Успешно удален пост");
  }

  @GetMapping("/get/{id}")
  public PostResponse get(@PathVariable(name = "id") String id) {
    var post = postService.findBy(id);
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
    return new PostResponse(post.getId(), post.getText(), post.getToUser());
  }
}
