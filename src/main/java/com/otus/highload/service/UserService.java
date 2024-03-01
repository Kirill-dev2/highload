package com.otus.highload.service;

import com.otus.highload.controller.request.LoginRequest;
import com.otus.highload.controller.request.RegisterUser;
import com.otus.highload.dao.User;
import com.otus.highload.exception.PasswordNotMatch;
import com.otus.highload.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public User createUser(RegisterUser registerUser) {
    log.debug("start create new user");
    var user = buildUser(registerUser);

    userRepository.save(user);
    log.info("created user.id [{}]", user.getId());
    return user;
  }

  public User findById(String id) {
    return userRepository.findById(id);
  }

  public User verify(LoginRequest request) {
    var user = userRepository.findByEmail(request.email());
    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      log.warn("user with id {} found, but passwords not matches", user.getId());
      throw new PasswordNotMatch("user not found");
    }
    return user;
  }

  private User buildUser(RegisterUser registerUser) {
    var user = new User();
    user.setId(UUID.randomUUID().toString());
    user.setCreated(LocalDateTime.now());
    user.setFirstName(registerUser.firstName());
    user.setSecondName(registerUser.secondName());
    user.setEmail(registerUser.email());
    user.setGender(registerUser.gender());
    user.setBirthdate(registerUser.birthdate());
    user.setBiography(registerUser.biography());
    user.setCity(registerUser.city());
    user.setPassword(passwordEncoder.encode(registerUser.password()));
    return user;
  }
}
