package org.otus.highload.core.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.otus.highload.core.controller.request.UpdatePostRequest;
import org.otus.highload.core.dao.Post;
import org.otus.highload.core.repository.PostRepositoryMaster;
import org.otus.highload.core.repository.PostRepositorySlave;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
  private final PostRepositoryMaster postRepositoryMaster;
  private final PostRepositorySlave postRepositorySlave;

  public Post create(String userId, String text) {
    var post = buildPost(text, userId);
    postRepositoryMaster.save(post);
    log.info("create new post on user.id [{}]", userId);
    return post;
  }

  public void updateBy(String userId, UpdatePostRequest request) {
    var post = this.findByAndUserId(userId, request.id());
    post.setText(request.text());
    post.setUpdated(LocalDateTime.now());
    postRepositoryMaster.updateTextById(post);
  }

  public void deleteBy(String id) {
    postRepositoryMaster.deleteById(id);
  }

  public Post findByAndUserId(String userId, String id) {
    return postRepositorySlave.findByIdAndUserId(userId, id);
  }

  public Post findBy(String userId, String id) {
    return postRepositorySlave.findByIdAndToUserId(userId, id);
  }

  public List<Post> findFirstAt(String userId, Long offset, Long limit) {
    return postRepositorySlave.findLastPosts(userId, offset, limit);
  }

  private Post buildPost(String text, String userId) {
    var now = LocalDateTime.now();
    var post = new Post();
    post.setId(UUID.randomUUID().toString());
    post.setCreated(now);
    post.setUpdated(now);
    post.setText(text);
    post.setUserId(userId);
    return post;
  }
}
