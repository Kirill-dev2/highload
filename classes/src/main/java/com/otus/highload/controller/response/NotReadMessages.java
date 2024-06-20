package com.otus.highload.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotReadMessages(@JsonProperty("messagesIds") List<String> messagesIds) {}
