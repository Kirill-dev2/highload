package com.otus.highload.core.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PostResponse(
    @JsonProperty("id") String id,
    @JsonProperty("text") String text,
    @JsonProperty("author_user_id") String toUserId)
    implements Serializable {}
