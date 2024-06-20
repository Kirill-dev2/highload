package com.otus.highload.core.feign;

import com.otus.highload.controller.dictionary.WorkflowStatuses;
import com.otus.highload.controller.response.NotReadMessages;
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
    value = "counter-service",
    url = "${counter-service.url}/api/v1/counter/",
    configuration = HeaderConfig.class)
public interface CounterClient {
  @PostMapping(value = "{user_id}/add")
  ResponseEntity<String> unread(
      @RequestHeader("message-id") String messageId,
      @RequestHeader("X-Auth-User") String userId,
      @PathVariable("user_id") String toUser);

  @PutMapping(value = "update/{id}/{status}")
  ResponseEntity<String> updateStatus(
      @PathVariable("id") String messageId, @PathVariable("status") WorkflowStatuses status);

  @PutMapping(value = "update/{status}")
  ResponseEntity<String> batchUpdate(
      @PathVariable("status") WorkflowStatuses status, @RequestBody Set<String> ids);

  @GetMapping(value = "unread")
  NotReadMessages getUndearMessages(@RequestHeader("X-Auth-User") String userId);

  @GetMapping(value = "{user_id}/unread")
  NotReadMessages getUndearMessagesFromUser(
      @RequestHeader("X-Auth-User") String userId, @PathVariable("user_id") String fromUser);
}
