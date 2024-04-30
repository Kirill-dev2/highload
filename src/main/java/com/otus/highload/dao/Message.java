package com.otus.highload.dao;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@Table(name = "messages")
@FieldNameConstants(asEnum = true)
public class Message {
  private String id;
  private LocalDateTime created;
  private LocalDateTime updated;
  private String text;
  private String fromUser;
  private String toUser;
  private int hash;
}
