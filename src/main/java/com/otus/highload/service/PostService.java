package com.otus.highload.service;

import com.otus.highload.controller.request.CreatePostRequest;
import com.otus.highload.controller.request.UpdatePostRequest;
import com.otus.highload.dao.Post;
import com.otus.highload.exception.UserNotFound;
import com.otus.highload.repository.PostRepositoryMaster;
import com.otus.highload.repository.PostRepositorySlave;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
  private final UserService service;
  private final PostRepositoryMaster postRepositoryMaster;
  private final PostRepositorySlave postRepositorySlave;

  public Post create(String userId, CreatePostRequest request) {
    if (!service.existById(request.toUser())) {
      throw new UserNotFound("user with ID: " + request.toUser() + " not exist");
    }

    var post = buildPost(request, userId);
    postRepositoryMaster.save(post);
    log.info("create new post on user.id [{}]", userId);
    return post;
  }

  public void updateBy(UpdatePostRequest request) {
    var post = this.findBy(request.id());
    post.setText(request.text());
    post.setUpdated(LocalDateTime.now());
    postRepositoryMaster.updateTextById(post);
  }

  public void deleteBy(String id) {
    postRepositoryMaster.deleteById(id);
  }

  public Post findBy(String id) {
    return postRepositorySlave.findById(id);
  }

  public List<Post> findFirstAt(String userId, Long offset, Long limit) {
    return postRepositorySlave.findLastPosts(userId, offset, limit);
  }

  private Post buildPost(CreatePostRequest request, String userId) {
    var now = LocalDateTime.now();
    var post = new Post();
    post.setId(UUID.randomUUID().toString());
    post.setCreated(now);
    post.setUpdated(now);
    post.setText(request.text());
    post.setFromUser(userId);
    post.setToUser(request.toUser());
    return post;
  }
}
