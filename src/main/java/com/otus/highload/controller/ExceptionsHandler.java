package com.otus.highload.controller;

import com.otus.highload.exception.PasswordNotMatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ExceptionsHandler {

  @ExceptionHandler(EmptyResultDataAccessException.class)
  public ResponseEntity<Void> handle(EmptyResultDataAccessException e) {
    log.warn(e.getMessage());
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(PasswordNotMatch.class)
  public ResponseEntity<Void> handle(PasswordNotMatch e) {
    return ResponseEntity.notFound().build();
  }
}
