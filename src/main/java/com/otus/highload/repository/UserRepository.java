package com.otus.highload.repository;

import com.otus.highload.dao.User;
import com.otus.highload.dao.User.Fields;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository extends AbstractRepository<User> {

  private static final Set<String> FIELDS =
      Arrays.stream(User.Fields.values()).map(Enum::name).collect(Collectors.toSet());

  UserRepository(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate, FIELDS);
  }

  public User findById(String id) {
    return super.findBy(FIELDS, Map.of(new ConditionArgs(Condition.EQUALS, Fields.id.name()), id));
  }

  public List<User> findAllByFirstNameLikeAndSecondNameLike(String firstName, String secondName) {
    return super.findAllByLike(
        FIELDS,
        Map.of(
            new ConditionArgs(Condition.LIKE, Fields.firstName.name()),
            firstName,
            new ConditionArgs(Condition.LIKE, Fields.secondName.name()),
            secondName));
  }

  public User findByEmail(String email) {
    return super.findBy(
        FIELDS, Map.of(new ConditionArgs(Condition.EQUALS, Fields.email.name()), email));
  }

  public boolean existByEmail(String email) {
    return super.existBy(Map.of(Fields.email.name(), email));
  }
}
