package com.otus.highload.chat.service;

import com.otus.highload.chat.dao.Message;
import com.otus.highload.chat.repository.MessageRepository;
import com.otus.highload.controller.dictionary.WorkflowStatuses;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
  private final MessageRepository messageRepository;

  public void create(String id, String userId, String toUser, String text) {
    var message = buildMessage(id, userId, toUser, text);
    messageRepository.save(message);
    log.info("create new message.id [{}]", id);
  }

  private Message buildMessage(String id, String userId, String toUser, String text) {
    var now = LocalDateTime.now();

    var message = new Message();
    message.setId(id);
    message.setCreated(now);
    message.setUpdated(now);
    message.setFromUser(userId);
    message.setToUser(toUser);
    message.setText(text);
    message.setHash(userId.hashCode() + toUser.hashCode());
    message.setWorkflowStatus(WorkflowStatuses.NEW.ordinal());
    return message;
  }

  public List<Message> findAllMessagesFromUserToUser(String userId, String toUser) {
    return messageRepository.findAllByHash(userId.hashCode() + toUser.hashCode());
  }

  public void updateStatus(String id, WorkflowStatuses status) {
    messageRepository.updateStatusById(id, status);
    log.debug("Update message.id [{}] to status {}", id, status);
  }

  public void batchUpdateStatus(Set<String> ids, WorkflowStatuses status) {
    messageRepository.updateStatusByIds(ids, status);
    log.debug("Update message.ids {} to status {}", ids, status);
  }
}
