package com.otus.highload.core.dao;

import com.otus.highload.dao.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@Table(name = "posts")
@FieldNameConstants(asEnum = true)
public class Post {
  private String id;
  private LocalDateTime created;
  private LocalDateTime updated;
  private String text;
  private String userId;
}
