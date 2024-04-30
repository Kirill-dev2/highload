package com.otus.highload.repository;

import com.otus.highload.dao.Message;
import com.otus.highload.dao.Message.Fields;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class MessageRepository extends AbstractRepository<Message> {

  private static final Set<String> FIELDS =
      Arrays.stream(Fields.values()).map(Enum::name).collect(Collectors.toSet());

  MessageRepository(@Qualifier("shardingJdbcTemplate") JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate, FIELDS);
  }

  public List<Message> findAllByHash(int hash) {
    return super.findAllBy(
        FIELDS, Map.of(new ConditionArgs(Condition.EQUALS, Fields.hash.name()), hash));
  }
}
