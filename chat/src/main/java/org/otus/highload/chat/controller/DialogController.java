package org.otus.highload.chat.controller;

import com.otus.highload.controller.request.DialogMessageText;
import com.otus.highload.controller.response.DialogMessage;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.otus.highload.chat.dao.Message;
import org.otus.highload.chat.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/dialog")
@RequiredArgsConstructor
public class DialogController {
  private final MessageService messageService;

  @PostMapping("/{user_id}/send")
  public ResponseEntity<String> sendMessageToUser(
      @RequestHeader("X-Auth-User") String userId,
      @Validated @NotBlank @PathVariable("user_id") String toUser,
      @Validated @RequestBody DialogMessageText messageText) {
    messageService.create(userId, toUser, messageText.text());
    return ResponseEntity.ok("Успешно отправлено сообщение");
  }

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
