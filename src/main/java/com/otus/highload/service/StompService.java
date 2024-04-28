package com.otus.highload.service;

import com.otus.highload.controller.response.PostResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class StompService {
  private final FriendService friendService;
  private final SimpMessagingTemplate simpMessagingTemplate;
  private final Map<String, String> destinationMap = new ConcurrentHashMap<>();

  @EventListener(SessionSubscribeEvent.class)
  public void handleSessionConnectEvent(SessionSubscribeEvent event) {
    var destination = SimpMessageHeaderAccessor.wrap(event.getMessage()).getDestination();
    var userId = determinateUserId(event);
    log.info("user.id [{}] Subscribe on [{}]", userId, destination);
    destinationMap.computeIfAbsent(userId, k -> destination);
  }

  @EventListener(SessionUnsubscribeEvent.class)
  public void handleSessionConnectEvent(SessionUnsubscribeEvent event) {
    var userId = determinateUserId(event);
    var destination = destinationMap.remove(userId);
    log.info("user.id [{}] unsubscribe on [{}]", userId, destination);
  }

  @EventListener(SessionConnectedEvent.class)
  public void handleSessionConnected(SessionConnectedEvent event) {
    var userId = determinateUserId(event);
    var destination = destinationMap.remove(userId);
    log.info("user.id [{}] unsubscribe on [{}]", userId, destination);
  }

  @EventListener(SessionDisconnectEvent.class)
  public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
    var userId = determinateUserId(event);
    var destination = destinationMap.remove(userId);
    log.info("user.id [{}] unsubscribe on [{}]", userId, destination);
  }

  public void sendToFriends(String userId, PostResponse post) {
    var friends = friendService.findAllFriendBy(userId);
    var stream = friends.size() > 500 ? friends.parallelStream() : friends.stream();
    var friendIds =
        stream.flatMap(f -> Stream.of(f.getUserId(), f.getFriendId())).collect(Collectors.toSet());

    log.info("post.id [{}], send all friends {}", post.id(), friendIds);

    friendIds.forEach(
        id -> {
          var destination = destinationMap.computeIfAbsent(id, k -> "/queue/friends.posts." + id);
          simpMessagingTemplate.convertAndSend(destination, post);
        });
  }

  private String determinateUserId(AbstractSubProtocolEvent event) {
    var user = (UsernamePasswordAuthenticationToken) event.getUser();
    if (user != null && user.getPrincipal() instanceof String userId) {
      return userId;
    } else {
      throw new AccessDeniedException("user mot Authenticated");
    }
  }
}
