package org.otus.highload.core.controller;

import com.otus.highload.controller.request.DialogMessageText;
import com.otus.highload.controller.response.DialogMessage;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.otus.highload.core.feign.DialogClient;
import org.otus.highload.core.security.AuthenticationUtil;
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

  @PostMapping("/{user_id}/send")
  public ResponseEntity<String> sendMessageToUser(
      @Validated @NotBlank @PathVariable("user_id") String toUser,
      @Validated @RequestBody DialogMessageText messageText) {
    var userId = AuthenticationUtil.extractUserId();
    return dialogClient.sendMessageToUser(userId, toUser, messageText);
  }

  @GetMapping("/{user_id}/list")
  public List<DialogMessage> getMessages(@Validated @PathVariable("user_id") String fromUser) {
    var toUser = AuthenticationUtil.extractUserId();
    return dialogClient.getMessages(toUser, fromUser);
  }
}
