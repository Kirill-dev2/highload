package com.otus.highload.controller;

import com.otus.highload.exception.FriendException;
import com.otus.highload.exception.PasswordNotMatch;
import com.otus.highload.exception.UserNotFound;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class ExceptionsHandler {

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Void> handle(NoResourceFoundException e) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Void> handle(AccessDeniedException e) {
    log.warn(e.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Void> handle(MethodArgumentNotValidException e) {
    log.warn(e.getMessage());
    return ResponseEntity.badRequest().build();
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Void> handle(HttpMessageNotReadableException e) {
    log.warn(e.getMessage());
    return ResponseEntity.badRequest().build();
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<Void> handle(HandlerMethodValidationException e) {
    log.warn(e.getMessage());
    return ResponseEntity.badRequest().build();
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Void> handle(MissingServletRequestParameterException e) {
    log.warn(e.getMessage());
    return ResponseEntity.badRequest().build();
  }

  @ExceptionHandler(EmptyResultDataAccessException.class)
  public ResponseEntity<Void> handle(EmptyResultDataAccessException e) {
    log.warn(e.getMessage());
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(PasswordNotMatch.class)
  public ResponseEntity<Void> handle(PasswordNotMatch e) {
    log.warn(e.getMessage());
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(UserNotFound.class)
  public ResponseEntity<Void> handle(UserNotFound e) {
    log.warn(e.getMessage());
    return ResponseEntity.badRequest().build();
  }

  @ExceptionHandler(FriendException.class)
  public ResponseEntity<Void> handle(FriendException e) {
    log.warn(e.getMessage());
    return ResponseEntity.badRequest().build();
  }
}
