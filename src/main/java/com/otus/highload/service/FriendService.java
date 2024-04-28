package com.otus.highload.service;

import com.otus.highload.dao.Friend;
import com.otus.highload.exception.FriendException;
import com.otus.highload.exception.UserNotFound;
import com.otus.highload.repository.FriendRepositoryMaster;
import com.otus.highload.repository.FriendRepositorySlave;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendService {
  private final UserService service;
  private final FriendRepositoryMaster friendRepositoryMaster;
  private final FriendRepositorySlave friendRepositorySlave;

  public void create(String userId, String friendId) {
    validation(userId, friendId);
    var friend = buildFriendRelationships(userId, friendId);
    friendRepositoryMaster.save(friend);
    log.info("user.id [{}] add new friend.id [{}]", userId, friendId);
  }

  private void validation(String userId, String friendId) {
    if (!service.existById(friendId)) {
      throw new UserNotFound("user with ID: " + friendId + " not exist");
    }

    if (friendRepositorySlave.existBy(userId, friendId)) {
      throw new FriendException("user ID: " + userId + " already friend with user ID: " + friendId);
    }
  }

  public void delete(String userId, String friendId) {
    if (!friendRepositorySlave.existBy(userId, friendId)) {
      throw new FriendException("user ID: " + userId + " not exist friend ID: " + friendId);
    }

    friendRepositoryMaster.delete(userId, friendId);
    log.info("user.id [{}] remove friend.id [{}]", userId, friendId);
  }

  private Friend buildFriendRelationships(String userId, String friendId) {
    var friend = new Friend();
    friend.setCreated(LocalDateTime.now());
    friend.setUserId(userId);
    friend.setFriendId(friendId);
    return friend;
  }

  public List<Friend> findAllFriendBy(String userId) {
    return friendRepositorySlave.findAllUserId(userId);
  }
}
