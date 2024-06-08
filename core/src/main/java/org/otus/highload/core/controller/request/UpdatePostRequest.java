package org.otus.highload.core.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdatePostRequest(
    @JsonProperty("id") @NotBlank String id, @JsonProperty("text") @NotBlank String text) {}
