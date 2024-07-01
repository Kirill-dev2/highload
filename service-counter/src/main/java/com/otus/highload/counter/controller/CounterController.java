package com.otus.highload.counter.controller;

import com.otus.highload.controller.dictionary.WorkflowStatuses;
import com.otus.highload.counter.service.CounterService;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/api/v1/counter")
@RequiredArgsConstructor
public class CounterController {
  private final CounterService counterService;

  @PostMapping("/{user_id}/add")
  public ResponseEntity<String> add(
      @RequestHeader("X-Auth-User") String userId,
      @RequestHeader("message-id") String messageId,
      @Validated @NotBlank @PathVariable("user_id") String toUser) {
    counterService.createNew(messageId, userId, toUser);
    return ResponseEntity.ok("Сообщение создано");
  }

  @PutMapping("/update/{id}/{status}")
  public ResponseEntity<String> update(
      @Validated @NotBlank @PathVariable("id") String messageID,
      @Validated @PathVariable("status") WorkflowStatuses status) {
    counterService.updateStatus(messageID, status);
    return ResponseEntity.ok("Статус изменен: " + status.name());
  }

  @PutMapping("/update/{status}")
  public ResponseEntity<String> batchUpdate(
      @Validated @PathVariable("status") WorkflowStatuses status, @RequestBody Set<String> ids) {
    counterService.batchUpdateStatus(ids, status);
    return ResponseEntity.ok("Статусы изменены: " + status.name());
  }

  @GetMapping("/unread/")
  public Long getAllUnReadedMessage(@RequestHeader("X-Auth-User") String userId) {
    return counterService.findAllUnreadByUserId(userId);
  }

  @GetMapping("/{user_id}/unread/")
  public Long getAllUnReadedMessage(
      @RequestHeader("X-Auth-User") String userId,
      @Validated @NotBlank @PathVariable("user_id") String fromUser) {
    return counterService.findAllUnreadByFromUserAndUserId(userId, fromUser);
  }
}
