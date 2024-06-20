package com.otus.highload.counter.repository;

import com.otus.highload.controller.dictionary.WorkflowStatuses;
import com.otus.highload.counter.dao.Counter;
import com.otus.highload.counter.dao.Counter.Fields;
import com.otus.highload.repository.AbstractRepository;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class CounterRepositorySlave extends AbstractRepository<Counter> {

  private static final Set<String> FIELDS =
      Arrays.stream(Fields.values()).map(Enum::name).collect(Collectors.toSet());

  CounterRepositorySlave(@Qualifier("slaveJdbcTemplate") JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate, FIELDS);
  }

  public Long findAllUnreadByUserId(String toUser) {
    var sql = "SELECT COUNT(*) FROM " + tableName + " WHERE workflowstatus = ? AND toUser = ? ;";
    var result =
        jdbcTemplate.queryForObject(sql, Long.class, WorkflowStatuses.SENT.ordinal(), toUser);
    log.debug("after execute SQL [{}], {} row delete", sql, result);
    return result;
  }

  public Long findAllUnreadByFromUserAndUserId(String toUser, String fromUser) {
    var sql =
        "SELECT COUNT(*) FROM "
            + tableName
            + " WHERE workflowstatus = ? AND toUser = ? AND fromUser = ?;";
    var result =
        jdbcTemplate.queryForObject(
            sql, Long.class, WorkflowStatuses.SENT.ordinal(), toUser, fromUser);
    log.debug("after execute SQL [{}], {} row delete", sql, result);
    return result;
  }
}
