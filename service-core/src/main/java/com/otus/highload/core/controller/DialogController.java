package com.otus.highload.core.controller;

import com.otus.highload.controller.dictionary.WorkflowStatuses;
import com.otus.highload.controller.request.DialogMessageText;
import com.otus.highload.controller.response.DialogMessage;
import com.otus.highload.core.feign.CounterClient;
import com.otus.highload.core.feign.DialogClient;
import com.otus.highload.core.security.AuthenticationUtil;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/dialog")
@RequiredArgsConstructor
public class DialogController {
  private final DialogClient dialogClient;
  private final CounterClient counterClient;

  @PostMapping("/{user_id}/send")
  public ResponseEntity<String> sendMessageToUser(
      @Validated @NotBlank @PathVariable("user_id") String toUser,
      @Validated @RequestBody DialogMessageText messageText) {
    var userId = AuthenticationUtil.extractUserId();
    var messageId = UUID.randomUUID().toString();

    try {
      var resultCreateMessage =
          CompletableFuture.completedFuture(
                  dialogClient.sendMessageToUser(messageId, userId, toUser, messageText))
              .thenApply(ResponseEntity::getStatusCode);
      var resultCreateCounter =
          CompletableFuture.completedFuture(counterClient.unread(messageId, userId, toUser))
              .thenApply(ResponseEntity::getStatusCode);

      var isSuccessful =
          CompletableFuture.allOf(resultCreateMessage, resultCreateCounter)
              .thenApply(
                  v ->
                      resultCreateMessage.join().is2xxSuccessful()
                          && resultCreateCounter.join().is2xxSuccessful())
              .exceptionally(e -> false);

      if (Boolean.TRUE.equals(isSuccessful.get())) {
        update(messageId, WorkflowStatuses.SENT);
        return ResponseEntity.ok("Успешно отправлено сообщение");
      } else {
        return reject(messageId);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return reject(messageId);
    }
  }

  private ResponseEntity<String> reject(String messageId) {
    update(messageId, WorkflowStatuses.REJECT);
    return ResponseEntity.status(HttpStatus.CONFLICT).body("Сообщение не отправлено");
  }

  private void update(String messageId, WorkflowStatuses status) {
    CompletableFuture.runAsync(() -> dialogClient.updateStatus(messageId, status));
    CompletableFuture.runAsync(() -> counterClient.updateStatus(messageId, status));
  }

  @GetMapping("/{user_id}/list")
  public List<DialogMessage> getMessages(@Validated @PathVariable("user_id") String fromUser) {
    var toUser = AuthenticationUtil.extractUserId();
    return dialogClient.getMessages(toUser, fromUser);
  }

  @PostMapping("/read/{message_id}/")
  public void readMessage(@Validated @PathVariable("message_id") String messageId) {
    try {
      var resultCreateMessage =
          CompletableFuture.completedFuture(
                  dialogClient.updateStatus(messageId, WorkflowStatuses.READ))
              .thenApply(ResponseEntity::getStatusCode);
      var resultCreateCounter =
          CompletableFuture.completedFuture(
                  counterClient.updateStatus(messageId, WorkflowStatuses.READ))
              .thenApply(ResponseEntity::getStatusCode);

      var isSuccessful =
          CompletableFuture.allOf(resultCreateMessage, resultCreateCounter)
              .thenApply(
                  v ->
                      resultCreateMessage.join().is2xxSuccessful()
                          && resultCreateCounter.join().is2xxSuccessful())
              .exceptionally(e -> false);

      if (Boolean.FALSE.equals(isSuccessful.get())) {
        update(messageId, WorkflowStatuses.SENT);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      update(messageId, WorkflowStatuses.SENT);
    }
  }
}
