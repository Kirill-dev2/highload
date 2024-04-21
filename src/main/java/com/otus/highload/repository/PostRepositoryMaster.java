package com.otus.highload.repository;

import com.otus.highload.dao.Post;
import com.otus.highload.dao.Post.Fields;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class PostRepositoryMaster extends AbstractRepository<Post> {

  private static final Set<String> FIELDS =
      Arrays.stream(Fields.values()).map(Enum::name).collect(Collectors.toSet());

  PostRepositoryMaster(@Qualifier("masterJdbcTemplate") JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate, FIELDS);
  }

  public void updateTextById(Post post) {
    super.update(post, Set.of(Fields.text.name(), Fields.updated.name()), Fields.id.name());
  }

  public void deleteById(String id) {
    super.delete(Map.of(Fields.id.name(), id));
  }
}
