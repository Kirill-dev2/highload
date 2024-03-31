package com.otus.highload.repository;

import com.otus.highload.dao.Table;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRepository<E> {
  private static final Collector<CharSequence, ?, String> COLLECTOR = Collectors.joining(",");
  private static final Collector<CharSequence, ?, String> AND = Collectors.joining(" AND ");
  private final Map<String, String> sqlCache = new ConcurrentHashMap<>();

  protected final BeanPropertyRowMapper<E> mapper;
  protected final JdbcTemplate jdbcTemplate;
  protected final NamedParameterJdbcTemplate parameterJdbcTemplate;
  protected final Set<String> tableFields;

  protected String tableName;

  AbstractRepository(JdbcTemplate jdbcTemplate, Set<String> tableFields) {
    this.jdbcTemplate = jdbcTemplate;
    this.tableFields = tableFields;
    this.parameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

    var typeClass = this.extractActualTypeClass();
    Optional.ofNullable(typeClass.getAnnotation(Table.class))
        .map(Table::name)
        .ifPresent(n -> this.tableName = n);
    this.mapper = BeanPropertyRowMapper.newInstance(typeClass);
  }

  protected E findBy(Set<String> fields, Map<ConditionArgs, Object> conditionWithArgs) {
    var conditions = conditionWithArgs.keySet();
    var sql =
        sqlCache.computeIfAbsent(
            "findBy_" + selectStatement(tableName, fields, conditions),
            k -> selectStatement(tableName, fields, conditions));
    var args = conditionWithArgs.values().toArray();
    var result = jdbcTemplate.queryForObject(sql, mapper, args);
    log.debug("result {} after execute SQL [{}] with param {}", result, sql, args);
    return result;
  }

  protected List<E> findAllByLike(
      Set<String> fields, Map<ConditionArgs, Object> conditionWithArgs) {
    var conditions = conditionWithArgs.keySet();
    var sql =
        sqlCache.computeIfAbsent(
            "findAllByLike" + likeStatement(tableName, fields, conditions),
            k -> likeStatement(tableName, fields, conditions));
    var args =
        conditionWithArgs.values().stream().map(o -> o + Condition.LIKE_PREFIX.value).toArray();
    var result = jdbcTemplate.query(sql, mapper, args);
    log.debug("result {} after execute SQL [{}] with param {}", result, sql, args);
    return result;
  }

  public void save(E entity) {
    var sql = sqlCache.computeIfAbsent("save", k -> insertStatement(tableName, tableFields));
    var result = insert(sql, entity);
    log.debug("after execute SQL [{}], {} row inserted", sql, result);
  }

  public boolean existBy(Map<String, Object> conditionWithArgs) {
    var condition = conditionWithArgs.keySet();
    var sql =
        sqlCache.computeIfAbsent(
            "existBy_" + countStatement(condition), k -> countStatement(condition));
    var args = conditionWithArgs.values().toArray();
    var count = jdbcTemplate.queryForObject(sql, Long.class, args);
    log.debug("count {} after execute SQL [{}] with param {}", count, sql, args);
    return count != null && count > 0;
  }

  private String selectStatement(
      String table, Set<String> fields, Collection<ConditionArgs> conditions) {
    return "SELECT %s FROM %s WHERE %s;"
        .formatted(
            fields.stream().map(String::toLowerCase).collect(COLLECTOR),
            table,
            conditions.stream().map(c -> c.field.toLowerCase() + c.condition.value).collect(AND));
  }

  private String likeStatement(
      String table, Set<String> fields, Collection<ConditionArgs> conditions) {
    return "SELECT %s FROM %s t WHERE %s ORDER BY t.id;"
        .formatted(
            fields.stream().map(String::toLowerCase).collect(COLLECTOR),
            table,
            conditions.stream().map(c -> c.field.toLowerCase() + c.condition.value).collect(AND));
  }

  private String insertStatement(String tableName, Set<String> fields) {
    return "INSERT INTO %s (%s) VALUES (%s);"
        .formatted(
            tableName,
            fields.stream().map(String::toLowerCase).collect(COLLECTOR),
            fields.stream().map(f -> ":" + f).collect(COLLECTOR));
  }

  private String countStatement(Collection<String> condition) {
    return "SELECT count(*) FROM %s t WHERE %s;"
        .formatted(
            tableName,
            condition.stream().map(c -> c.toLowerCase() + Condition.EQUALS.value).collect(AND));
  }

  private int insert(String sql, E entity) {
    try {
      return parameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(entity));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return 0;
  }

  private Class<E> extractActualTypeClass() {
    var classType =
        Optional.of(this.getClass())
            .map(Class::getGenericSuperclass)
            .filter(ParameterizedType.class::isInstance)
            .map(ParameterizedType.class::cast)
            .map(ParameterizedType::getActualTypeArguments)
            .stream()
            .flatMap(Arrays::stream)
            .findFirst()
            .orElseThrow(IllegalStateException::new);
    return (Class<E>) classType;
  }

  @AllArgsConstructor
  protected enum Condition {
    EQUALS(" = ?"),
    LIKE(" like ?"),
    LIKE_PREFIX("%");

    private final String value;
  }

  protected record ConditionArgs(Condition condition, String field) {}
}
