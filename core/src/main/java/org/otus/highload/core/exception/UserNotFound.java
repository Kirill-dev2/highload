package org.otus.highload.core.exception;

public class UserNotFound extends RuntimeException {

  public UserNotFound(String message) {
    super(message);
  }
}
