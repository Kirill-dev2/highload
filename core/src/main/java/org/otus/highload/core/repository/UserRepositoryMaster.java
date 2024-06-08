package org.otus.highload.core.repository;

import com.otus.highload.repository.AbstractRepository;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.otus.highload.core.dao.User;
import org.otus.highload.core.dao.User.Fields;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryMaster extends AbstractRepository<User> {

  private static final Set<String> FIELDS =
      Arrays.stream(User.Fields.values()).map(Enum::name).collect(Collectors.toSet());

  UserRepositoryMaster(@Qualifier("masterJdbcTemplate") JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate, FIELDS);
  }

  public User findByEmail(String email) {
    return super.findBy(
        FIELDS, Map.of(new ConditionArgs(Condition.EQUALS, Fields.email.name()), email));
  }

  public boolean existByEmail(String email) {
    return super.existBy(Map.of(Fields.email.name(), email));
  }
}
