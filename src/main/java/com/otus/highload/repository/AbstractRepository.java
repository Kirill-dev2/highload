package com.otus.highload.repository;

import com.otus.highload.dao.Table;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
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

  protected E findBy(Set<String> fields, Map<String, Object> conditionWithArgs) {
    var condition = conditionWithArgs.keySet().toArray(String[]::new);
    var sql = selectStatement(tableName, fields, condition);
    var args = conditionWithArgs.values().toArray();
    var result = jdbcTemplate.queryForObject(sql, mapper, args);
    log.debug("result {} after execute SQL [{}] with param {}", result, sql, args);
    return result;
  }

  public void save(E entity) {
    var sql = insertStatement(tableName, tableFields);
    var result = insert(sql, entity);
    log.debug("after execute SQL [{}], {} row inserted", sql, result);
  }

  public boolean existBy(Map<String, Object> conditionWithArgs) {
    var condition = conditionWithArgs.keySet().toArray(String[]::new);
    var sql =
        String.format(
            "SELECT count(*) FROM %s t WHERE %s;",
            tableName, Arrays.stream(condition).map(c -> c.toLowerCase() + "= ?").collect(AND));
    var args = conditionWithArgs.values().toArray();
    var count = jdbcTemplate.queryForObject(sql, Long.class, args);
    log.debug("count {} after execute SQL [{}] with param {}", count, sql, args);
    return count > 0;
  }

  private String selectStatement(String table, Set<String> fields, String... condition) {
    return String.format(
        "SELECT %s FROM %s WHERE %s;",
        fields.stream().map(String::toLowerCase).collect(COLLECTOR),
        table,
        Arrays.stream(condition).map(c -> c.toLowerCase() + "= ?").collect(AND));
  }

  private String insertStatement(String tableName, Set<String> fields) {
    return String.format(
        "INSERT INTO %s (%s) VALUES (%s);",
        tableName,
        fields.stream().map(String::toLowerCase).collect(COLLECTOR),
        fields.stream().map(f -> ":" + f).collect(COLLECTOR));
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
}
