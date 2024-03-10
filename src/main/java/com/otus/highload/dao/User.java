package com.otus.highload.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

@Data
@Table(name = "users")
@FieldNameConstants(asEnum = true)
public class User {
  private String id;
  private LocalDateTime created;
  private LocalDateTime updated;
  private String firstName;
  private String secondName;
  private String email;
  private String gender;
  private LocalDate birthdate;
  private String biography;
  private String city;
  @ToString.Exclude private String password;
}
