package com.otus.highload.repository;

import com.otus.highload.dao.Friend;
import com.otus.highload.dao.Friend.Fields;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class FriendRepositorySlave extends AbstractRepository<Friend> {

  private static final Set<String> FIELDS =
      Arrays.stream(Fields.values()).map(Enum::name).collect(Collectors.toSet());

  FriendRepositorySlave(@Qualifier("masterJdbcTemplate") JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate, FIELDS);
  }

  public boolean existBy(String userId, String friendId) {
    var sql =
        "SELECT count(*) FROM "
            + tableName
            + " WHERE userId = ? AND friendId = ? OR userId = ? AND friendId = ?;";
    var count = jdbcTemplate.queryForObject(sql, Long.class, userId, friendId, friendId, userId);
    log.debug(
        "count {} after execute SQL [{}] with param {}",
        count,
        sql,
        userId,
        friendId,
        friendId,
        userId);
    return count != null && count > 0;
  }

  public List<Friend> findAllUserId(String userId) {
    var sql = "SELECT * FROM " + tableName + " WHERE friendId = ? OR userId = ?;";
    return jdbcTemplate.query(sql, mapper, userId, userId);
  }
}
