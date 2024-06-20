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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class CounterRepositoryMaster extends AbstractRepository<Counter> {

  private static final Set<String> FIELDS =
      Arrays.stream(Fields.values()).map(Enum::name).collect(Collectors.toSet());

  CounterRepositoryMaster(@Qualifier("masterJdbcTemplate") JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate, FIELDS);
  }

  public void updateStatusById(String id, WorkflowStatuses status) {
    var sql = "UPDATE " + tableName + " SET workflowstatus = ? WHERE id = ?;";
    var result = jdbcTemplate.update(sql, status.ordinal(), id);
    log.debug("after execute SQL [{}], {} row delete", sql, result);
  }

  public void updateStatusByIds(Set<String> ids, WorkflowStatuses status) {
    var sql = "UPDATE " + tableName + " SET workflowstatus = :status WHERE id IN (:ids);";

    var parameters = new MapSqlParameterSource();
    parameters.addValue("ids", ids);
    parameters.addValue("status", status.ordinal());

    var result = parameterJdbcTemplate.update(sql, parameters);
    log.debug("after execute SQL [{}], {} row delete", sql, result);
  }
}
