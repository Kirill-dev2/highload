package org.otus.highload.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.otus.highload.chat.dao.Message;
import org.otus.highload.chat.repository.MessageRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
  private final MessageRepository messageRepository;

  public void create(String userId, String toUser, String text) {
    var message = buildMessage(userId, toUser, text);
    messageRepository.save(message);
    log.info("create new message.id [{}]", message.getId());
  }

  private Message buildMessage(String userId, String toUser, String text) {
    var now = LocalDateTime.now();

    var message = new Message();
    message.setId(UUID.randomUUID().toString());
    message.setCreated(now);
    message.setUpdated(now);
    message.setFromUser(userId);
    message.setToUser(toUser);
    message.setText(text);
    message.setHash(userId.hashCode() + toUser.hashCode());
    return message;
  }

  public List<Message> findAllMessagesFromUserToUser(String userId, String toUser) {
    return messageRepository.findAllByHash(userId.hashCode() + toUser.hashCode());
  }
}
