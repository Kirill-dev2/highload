package org.otus.highload.core.repository;

import com.otus.highload.repository.AbstractRepository;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.otus.highload.core.dao.Friend;
import org.otus.highload.core.dao.Friend.Fields;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class FriendRepositoryMaster extends AbstractRepository<Friend> {

  private static final Set<String> FIELDS =
      Arrays.stream(Fields.values()).map(Enum::name).collect(Collectors.toSet());

  FriendRepositoryMaster(@Qualifier("masterJdbcTemplate") JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate, FIELDS);
  }

  public void delete(String userId, String friendId) {
    var sql =
        "DELETE FROM "
            + tableName
            + " WHERE userId = ? AND friendId = ? OR userId = ? AND friendId = ?;";
    var result = jdbcTemplate.update(sql, userId, friendId, friendId, userId);
    log.debug("after execute SQL [{}], {} row delete", sql, result);
  }
}
