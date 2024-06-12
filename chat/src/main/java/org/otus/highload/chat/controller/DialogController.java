package org.otus.highload.chat.controller;

import com.otus.highload.controller.ApiMessages.DialogMessage;
import com.otus.highload.controller.ApiMessages.DialogMessageText;
import com.otus.highload.controller.ApiMessages.DialogMessages;
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

  @PostMapping(value = "/{user_id}/send", consumes = "application/x-protobuf", produces = "application/x-protobuf")
  public ResponseEntity<String> sendMessageToUser(
      @RequestHeader("X-Auth-User") String userId,
      @Validated @NotBlank @PathVariable("user_id") String toUser,
      @Validated @RequestBody DialogMessageText messageText) {
    messageService.create(userId, toUser, messageText.getText());
    return ResponseEntity.ok("Успешно отправлено сообщение");
  }

  @GetMapping(value = "/{user_id}/list", consumes = "application/x-protobuf", produces = "application/x-protobuf")
  public DialogMessages getMessages(
      @RequestHeader("X-Auth-User") String toUser,
      @Validated @PathVariable("user_id") String fromUser) {
    var messages = messageService.findAllMessagesFromUserToUser(fromUser, toUser);
    var stream = messages.size() > 500 ? messages.parallelStream() : messages.stream();
    var list = stream.map(this::buildResponse).toList();
    return DialogMessages.newBuilder().addAllMessages(list).build();
  }

  private DialogMessage buildResponse(Message message) {
    return DialogMessage.newBuilder().setFromUser(message.getFromUser()).setToUser(message.getToUser()).setText(message.getText()).build();
//    return new DialogMessage(message.getFromUser(), message.getToUser(), message.getText());
  }
}
