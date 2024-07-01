package com.otus.highload.core.feign;

import com.otus.highload.controller.dictionary.WorkflowStatuses;
import com.otus.highload.controller.request.DialogMessageText;
import com.otus.highload.controller.response.DialogMessage;
import java.util.List;
import java.util.Set;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    value = "dialog-service",
    url = "${dialog-service.url}/api/v1/dialog/",
    configuration = HeaderConfig.class)
public interface DialogClient {
  @PostMapping(value = "{user_id}/send")
  ResponseEntity<String> sendMessageToUser(
      @RequestHeader("message-id") String messageID,
      @RequestHeader("X-Auth-User") String userId,
      @PathVariable("user_id") String toUser,
      @RequestBody DialogMessageText messageText);

  @PutMapping(value = "update/{id}/{status}")
  ResponseEntity<String> updateStatus(
      @PathVariable("id") String messageId, @PathVariable("status") WorkflowStatuses status);

  @PutMapping(value = "update/{status}")
  ResponseEntity<String> batchUpdate(
      @PathVariable("status") WorkflowStatuses status, @RequestBody Set<String> ids);

  @GetMapping(value = "{user_id}/list")
  List<DialogMessage> getMessages(
      @RequestHeader("X-Auth-User") String toUser, @PathVariable("user_id") String fromUser);
}
