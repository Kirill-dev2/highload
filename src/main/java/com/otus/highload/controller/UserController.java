package com.otus.highload.controller;

import com.otus.highload.controller.request.RegisterUser;
import com.otus.highload.controller.response.UserProfile;
import com.otus.highload.dao.User;
import com.otus.highload.service.UserService;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @PostMapping("/register")
  public Map<String, String> register(@Validated @RequestBody RegisterUser registerUser) {
    var user = userService.createUser(registerUser);
    return Map.of("user_id", user.getId());
  }

  @GetMapping("/get/{id}")
  public UserProfile getById(@Validated @NotBlank @PathVariable String id) {
    var user = userService.findById(id);
    return fillUserProfile(user);
  }

  @GetMapping(value = "/search")
  public List<UserProfile> searchBy(
      @Validated @NotBlank @RequestParam("first_name") String firstName,
      @NotBlank @RequestParam("last_name") String secondName) {
    var users = userService.findBy(firstName, secondName);
    var stream = users.size() > 500 ? users.parallelStream() : users.stream();
    return stream.map(this::fillUserProfile).toList();
  }

  private UserProfile fillUserProfile(User user) {
    return new UserProfile(
        user.getId(),
        user.getFirstName(),
        user.getSecondName(),
        user.getGender(),
        user.getBirthdate(),
        user.getBiography(),
        user.getCity());
  }
}
