package com.otus.highload.controller;

import com.otus.highload.controller.request.LoginRequest;
import com.otus.highload.security.TokenProvider;
import com.otus.highload.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LoginController {
  private final UserService userService;
  private final TokenProvider tokenProvider;

  @PostMapping("/login")
  public Map<String, String> login(
      @Validated @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
    var user = userService.verify(request);
    return Map.of("token", tokenProvider.generateToken(user, servletRequest));
  }
}
