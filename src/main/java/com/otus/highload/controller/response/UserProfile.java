package com.otus.highload.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record UserProfile(
    @JsonProperty("id") String id,
    @JsonProperty("first_name") String firstName,
    @JsonProperty("second_name") String secondName,
    @JsonProperty("gender") String gender,
    @JsonProperty("birthdate") LocalDate birthdate,
    @JsonProperty("biography") String biography,
    @JsonProperty("city") String city) {}
