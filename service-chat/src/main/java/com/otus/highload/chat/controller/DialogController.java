package com.otus.highload.chat.controller;

import com.otus.highload.chat.dao.Message;
import com.otus.highload.chat.service.MessageService;
import com.otus.highload.controller.dictionary.WorkflowStatuses;
import com.otus.highload.controller.request.DialogMessageText;
import com.otus.highload.controller.response.DialogMessage;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/dialog")
@RequiredArgsConstructor
public class DialogController {
  private final MessageService messageService;

  @Timed
  @PostMapping("/{user_id}/send")
  public ResponseEntity<String> sendMessageToUser(
      @RequestHeader("X-Auth-User") String userId,
      @RequestHeader("message-id") String id,
      @Validated @NotBlank @PathVariable("user_id") String toUser,
      @Validated @RequestBody DialogMessageText messageText) {
    messageService.create(id, userId, toUser, messageText.text());
    return ResponseEntity.ok("Успешно отправлено сообщение");
  }

  @Timed
  @PutMapping("/update/{id}/{status}")
  public ResponseEntity<String> update(
      @Validated @NotBlank @PathVariable("id") String messageID,
      @Validated @PathVariable("status") WorkflowStatuses status) {
    messageService.updateStatus(messageID, status);
    return ResponseEntity.ok("Статус изменен: " + status.name());
  }

  @Timed
  @PutMapping("/update/{status}")
  public ResponseEntity<String> batchUpdate(
      @Validated @PathVariable("status") WorkflowStatuses status, @RequestBody Set<String> ids) {
    messageService.batchUpdateStatus(ids, status);
    return ResponseEntity.ok("Статус изменен: " + status.name());
  }

  @Timed
  @GetMapping("/{user_id}/list")
  public List<DialogMessage> getMessages(
      @RequestHeader("X-Auth-User") String toUser,
      @Validated @PathVariable("user_id") String fromUser) {
    var messages = messageService.findAllMessagesFromUserToUser(fromUser, toUser);
    var stream = messages.size() > 500 ? messages.parallelStream() : messages.stream();
    return stream.map(this::buildResponse).toList();
  }

  private DialogMessage buildResponse(Message message) {
    return new DialogMessage(message.getFromUser(), message.getToUser(), message.getText());
  }
}
