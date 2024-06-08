package org.otus.highload.core.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.otus.highload.core.security.AuthenticationUtil;
import org.otus.highload.core.service.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendController {
  private final FriendService friendService;

  @PutMapping("/set/{user_id}")
  public ResponseEntity<String> addFriend(@PathVariable(name = "user_id") String userId) {
    friendService.create(AuthenticationUtil.extractUserId(), userId);
    return ResponseEntity.ok("Пользователь успешно указал своего друга");
  }

  @PutMapping("/delete/{user_id}")
  public ResponseEntity<String> deleteFriend(@PathVariable(name = "user_id") String userId) {
    friendService.delete(AuthenticationUtil.extractUserId(), userId);
    return ResponseEntity.ok("Пользователь успешно удалил из друзей пользователя");
  }
}
