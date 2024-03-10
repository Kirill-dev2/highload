package com.otus.highload.exception;

public class PasswordNotMatch extends RuntimeException {

  public PasswordNotMatch(String message) {
    super(message);
  }
}
