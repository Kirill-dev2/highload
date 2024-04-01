# Производительность индеков

## Запрос для поиска анкет по префиксу имени и фамилии (одновременно)
```sql
SELECT * FROM social_network.users u WHERE u.firstname LIKE 'Мар%' AND u.secondname LIKE 'Сте%' ORDER BY u.id;
```
- План запроса без индекса
![explain_before_index.png](before_index%2Fexplain_before_index.png)


## Нагрузочные тесты с **jmeter**
Реализованы при помощи jmeter
[otus_highload_search.jmx](jmeter%2Fotus_highload_search.jmx)

#### Результаты без индексов
![latency_before_index.png](before_index%2Flatency_before_index.png)
![throughput_before_index.png](before_index%2Fthroughput_before_index.png)
![transactions_per_second_before_index.png](before_index%2Ftransactions_per_second_before_index.png)

## Создание индекса
```sql
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS first_name_last_name_idx_gin ON social_network.users USING gin (firstname gin_trgm_ops, secondname gin_trgm_ops);
```
- Из официальной документации PostgreSQL [textsearch-indexes](https://postgrespro.ru/docs/postgresql/16/textsearch-indexes) рекомендация использования индексов
`Для ускорения полнотекстового поиска можно использовать индексы двух видов:GIN и GiST` 

- План запроса после построения индекса
![explain_after_index.png](after_index%2Fexplain_after_index.png)


## Результаты после применения индексов
![latency_after_index.png](after_index%2Flatency_after_index.png)
![throughput_after_index.png](after_index%2Fthroughput_after_index.png)
![transactions_per_second_after_index.png](after_index%2Ftransactions_per_second_after_index.png)