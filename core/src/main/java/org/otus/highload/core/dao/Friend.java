package org.otus.highload.core.dao;

import com.otus.highload.dao.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

@Data
@Table(name = "friends")
@FieldNameConstants(asEnum = true)
public class Friend {
  @ToString.Exclude private LocalDateTime created;
  private String userId;
  private String friendId;
}
