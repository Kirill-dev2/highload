package org.otus.highload.core.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.otus.highload.core.controller.request.LoginRequest;
import org.otus.highload.core.controller.request.RegisterUser;
import org.otus.highload.core.dao.User;
import org.otus.highload.core.exception.PasswordNotMatch;
import org.otus.highload.core.repository.UserRepositoryMaster;
import org.otus.highload.core.repository.UserRepositorySlave;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepositoryMaster userRepositoryMaster;
  private final UserRepositorySlave userRepositorySlave;
  private final PasswordEncoder passwordEncoder;

  public User createUser(RegisterUser registerUser) {
    log.debug("start create new user");
    var user = buildUser(registerUser);

    userRepositoryMaster.save(user);
    log.info("created user.id [{}]", user.getId());
    return user;
  }

  public User findById(String id) {
    return userRepositorySlave.findById(id);
  }

  public boolean existByEmail(String email) {
    return userRepositorySlave.existByEmail(email);
  }

  public boolean existById(String id) {
    return userRepositorySlave.existById(id);
  }

  public List<User> findBy(String firstName, String secondName) {
    return userRepositorySlave.findAllByFirstNameLikeAndSecondNameLike(firstName, secondName);
  }

  public User verify(LoginRequest request) {
    var user = userRepositoryMaster.findByEmail(request.email());
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
