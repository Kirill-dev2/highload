package com.otus.highload.repository;

import com.otus.highload.dao.Post;
import com.otus.highload.dao.Post.Fields;
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
public class PostRepositorySlave extends AbstractRepository<Post> {

  private static final Set<String> FIELDS =
      Arrays.stream(Fields.values()).map(Enum::name).collect(Collectors.toSet());

  PostRepositorySlave(@Qualifier("masterJdbcTemplate") JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate, FIELDS);
  }

  public Post findByIdAndToUserId(String userId, String id) {
    var sql =
        """
        SELECT p.* FROM posts p
        JOIN friends f ON (p.userid = f.userid OR p.userid = f.friendid)
        WHERE p.id = ?
        AND p.userid != ?
        AND (f.userid = ? OR f.friendid = ?);
        """;
    var post = jdbcTemplate.queryForObject(sql, mapper, id, userId, userId, userId);
    log.debug(
        "result {} after execute SQL [{}] with id [{}], to user.id [{}]", post, sql, id, userId);
    return post;
  }

  public Post findByIdAndUserId(String userId, String id) {
    return super.findBy(
        FIELDS,
        Map.of(
            new ConditionArgs(Condition.EQUALS, Fields.userId.name()),
            userId,
            new ConditionArgs(Condition.EQUALS, Fields.id.name()),
            id));
  }

  public List<Post> findLastPosts(String userId, Long offset, Long limit) {
    var sql =
        """
            SELECT p.* FROM posts p
            JOIN friends f ON (p.userid = f.userid OR p.userid = f.friendid)
            WHERE p.userid != ?
            AND (f.userid = ? OR f.friendid = ?)
            ORDER BY p.updated DESC
            OFFSET ? LIMIT ?;
            """;
    var posts = jdbcTemplate.query(sql, mapper, userId, userId, userId, offset, limit);
    log.debug(
        "result {} after execute SQL [{}] with user.id {}, offset {}, limit {}",
        posts.size(),
        sql,
        userId,
        offset,
        limit);
    return posts;
  }
}
