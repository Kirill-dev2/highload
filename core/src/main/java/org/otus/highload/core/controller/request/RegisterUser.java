package org.otus.highload.core.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.otus.highload.core.validation.UniqueEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import org.hibernate.validator.constraints.Length;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterUser(
    @JsonProperty("first_name") @NotBlank @Length(max = 50) String firstName,
    @JsonProperty("second_name") @NotBlank @Length(max = 50) String secondName,
    @JsonProperty("email") @NotBlank @UniqueEmail @Length(max = 254) String email,
    @JsonProperty("birthdate") @NotNull LocalDate birthdate,
    @JsonProperty("gender") @NotNull @Pattern(regexp = "Мужской|Женский") String gender,
    @JsonProperty("biography") String biography,
    @JsonProperty("city") @Length(max = 40) String city,
    @JsonProperty("password") @NotBlank @Length(max = 60) String password) {

  @Override
  public String toString() {
    return "RegisterUser{" +
        "firstName='" + firstName + '\'' +
        ", secondName='" + secondName + '\'' +
        ", email='" + email + '\'' +
        ", birthdate=" + birthdate +
        ", gender='" + gender + '\'' +
        ", biography='" + biography + '\'' +
        ", city='" + city + '\'' +
        '}';
  }
}
