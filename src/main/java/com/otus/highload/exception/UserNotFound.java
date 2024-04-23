package com.otus.highload.exception;

public class UserNotFound extends RuntimeException {

  public UserNotFound(String message) {
    super(message);
  }
}
