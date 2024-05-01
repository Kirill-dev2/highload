# Шардирование
В качестве ключа шардирования будем использовать **hash** (large integer), 
hash рассчитывается на основе идентификаторов двух пользователей
Эффект ЛедиГаги здесь исключен, т.к. hash является числовым типом, has функция citus `id -> hash(id) % 32 -> shard -> worker` 
записи будут равномерно распределяться по всем шардам

### Шардинг
1) Создадим таблицу для хрананения сообщений между двумя пользователями
```CREATE TABLE IF NOT EXISTS chat.messages
CREATE TABLE IF NOT EXISTS chat.messages
(
    id        varchar(36)   NOT NULL,
    created   timestamp     NOT NULL,
    updated   timestamp     NULL,
    "text"    varchar       NOT NULL,
    fromuser  varchar(36)   NOT NULL,
    touser    varchar(36)   NOT NULL,
    hash      bigint        NOT NULL,
    CONSTRAINT messages_pkey PRIMARY KEY (id, hash)
);
```
2) Создадим из нее распределенную (шардированную) таблицу

`SELECT create_distributed_table('chat.messages', 'hash');`

3) Проверим, на каких узлах лежат сейчас данные:

`SELECT nodename, count(*) FROM citus_shards GROUP BY nodename;`

4) Посмотрим план запроса.

```
EXPLAIN analyze SELECT * FROM chat.messages m WHERE m.hash = 1163943307;
```
Видим, что такой select отправится только на один из шардов
```
QUERY PLAN                                                                                                               |
-------------------------------------------------------------------------------------------------------------------------+
Custom Scan (Citus Adaptive)  (cost=0.00..0.00 rows=0 width=0) (actual time=2.166..2.170 rows=21 loops=1)                |
  Task Count: 1                                                                                                          |
  Tuple data received from nodes: 2863 bytes                                                                             |
  Tasks Shown: All                                                                                                       |
  ->  Task                                                                                                               |
        Tuple data received from node: 2863 bytes                                                                        |
        Node: host=sharding-worker-1 port=5432 dbname=postgres                                                           |
        ->  Seq Scan on messages_102022 m  (cost=0.00..12.88 rows=1 width=326) (actual time=0.020..0.025 rows=21 loops=1)|
              Filter: (hash = 1163943307)                                                                                |
            Planning Time: 0.097 ms                                                                                      |
            Execution Time: 0.046 ms                                                                                     |
Planning Time: 0.166 ms                                                                                                  |
Execution Time: 2.200 ms                                                                                                 |                                                                       
```

### Решардинг
1) Добавим еще парочку шардов: 
используем [шаблон citus](https://github.com/citusdata/tools/blob/develop/packaging_automation/templates/docker/latest/docker-compose.tmpl.yml)

`POSTGRES_PASSWORD=pass docker-compose -p citus up --scale worker=5 -d`

2) Установим `wal_level = logical` чтобы узлы могли переносить данные:
```
alter system set wal_level = logical; 
SELECT run_command_on_workers('alter system set wal_level = logical');
```
3) Перезапускаем все узлы в кластере, чтобы применить изменения wal_level.
4) Проверим, что `wal_level` изменился у воркеров:
```
docker exec -it citus-worker-1 psql -U postgres
show wal_level;
```
5) Запустим ребалансировку:
```
docker exec -it citus_master psql -U postgres
SELECT citus_rebalance_start();
```
6) Проверим статус ребалансировки `SELECT * FROM citus_rebalance_status();`
7) Проверяем, что данные равномерно распределились по шардам:
```
SELECT nodename, count(*) FROM citus_shards GROUP BY nodename;
```

[additional link](https://github.com/a-poliakov/highload-arch-examples/tree/main/sharding/postgres)