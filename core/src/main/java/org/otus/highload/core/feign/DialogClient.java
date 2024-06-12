package org.otus.highload.core.feign;

import com.otus.highload.controller.ApiMessages.DialogMessage;
import com.otus.highload.controller.ApiMessages.DialogMessageText;
import com.otus.highload.controller.ApiMessages.DialogMessages;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    value = "dialog-service",
    url = "${dialog-service.url}",
    configuration = HeaderConfig.class)
public interface DialogClient {
  @PostMapping(value = "/dialog/{user_id}/send", consumes = "application/x-protobuf", produces = "application/x-protobuf")
  ResponseEntity<String> sendMessageToUser(
      @RequestHeader("X-Auth-User") String userId,
      @PathVariable("user_id") String toUser,
      @RequestBody DialogMessageText messageText);

  @GetMapping(value = "/dialog/{user_id}/list", consumes = "application/x-protobuf", produces = "application/x-protobuf")
  DialogMessages getMessages(
      @RequestHeader("X-Auth-User") String toUser, @PathVariable("user_id") String fromUser);
}
