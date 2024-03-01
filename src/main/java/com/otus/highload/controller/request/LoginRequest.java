package com.otus.highload.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginRequest(
    @JsonProperty("email") @NotBlank String email,
    @JsonProperty("password") @NotBlank String password) {

  @Override
  public String toString() {
    return "LoginRequest{" + "email='" + email + '\'' + '}';
  }
}
