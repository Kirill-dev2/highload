package com.otus.highload.counter.dao;

import com.otus.highload.controller.dictionary.WorkflowStatuses;
import com.otus.highload.dao.Table;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@Table(name = "counters")
@FieldNameConstants(asEnum = true)
public class Counter {
  private String id;
  private String fromUser;
  private String toUser;
  private int workflowStatus;

  public Counter(String id, String fromUser, String toUser) {
    this.id = id;
    this.fromUser = fromUser;
    this.toUser = toUser;
    this.workflowStatus = WorkflowStatuses.NEW.ordinal();
  }
}
