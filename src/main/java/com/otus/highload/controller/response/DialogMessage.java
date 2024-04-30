package com.otus.highload.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DialogMessage(
    @JsonProperty("from") String fromUser,
    @JsonProperty("to") String toUser,
    @JsonProperty("text") String text) {}
