# Репликация

### Настройка на мастере

1) Подключаемся к docker контейнеру: `docker exec -it db-master bash`
2) Заходим под пользователем postgres: `su - postgres`
3) Создаем пользователя, под которым будем подключаться со стороны вторичного сервера: `createuser --replication -P repluser`
4) Открываем конфигурационный файл _postgresql.conf_: `vi /var/lib/postgresql/data/postgresql.conf`
Приводим к следующием виду некоторые параметры:

```
wal_level = replica
max_wal_senders = 4
max_replication_slots = 4
hot_standby = on
hot_standby_feedback = on
```

```
* wal_level указывает, сколько информации записывается в WAL (журнал операций, который используется для репликации). Значение replica указывает на необходимость записывать только данные для поддержки архивирования WAL и репликации.
* max_wal_senders — количество планируемых слейвов;
* max_replication_slots — максимальное число слотов репликации (данный параметр не нужен для postgresql 9.2 — с ним сервер не запустится);
* hot_standby — определяет, можно или нет подключаться к postgresql для выполнения запросов в процессе восстановления;
* hot_standby_feedback — определяет, будет или нет сервер slave сообщать мастеру о запросах, которые он выполняет.
```

5) Добавляем запись в _pg_hba.conf_ адрес сети, созданной из _docker-compose_.
    1) Находим адрес сети: `docker network inspect highload_default | grep Subnet`
    2) Добавляем запись: `host    replication     replicator       192.168.192.0/20          md5`
    3) Перезапускаем мастер: `docker restart db-master`

### Настройка на слейве

1) Подключаемся к docker контейнеру: `docker exec -it db-slave bash`
2) удалим содержимое рабочего каталога вторичной базы: `rm -r /var/lib/postgresql/data/*`
3) Выполняем команду: 
 ```
pg_basebackup --host=db-master --username=repluser --pgdata=/var/lib/postgresql/data --wal-method=stream --write-recovery-conf 
 ```
 ```
* db-maste — мастер; /var/lib/postgresql/data — путь до каталога с данными слейва.
```
##### Проверка
1) Смотрим статус работы мастера:
`docker exec -it db-master su - postgres -c "psql -c 'select * from pg_stat_replication;'"`
2) Смотрим статус работы слейва:
`docker exec -it db-slave su - postgres -c "psql -c 'select * from pg_stat_wal_receiver;'"`


### Замеряем нагрузку на мастере до перевода запросов на слейв
Запускаем нагрузочный тест [(JMeter)](otus_highload_test_plan.jmx)
![before.png](before_replica%2Fbefore.png)
![before_1.png](before_replica%2Fbefore_1.png)
![before_2.png](before_replica%2Fbefore_2.png)
![before_3.png](before_replica%2Fbefore_3.png)

### Замеряем нагрузку на мастере после перевода запросов на слейв
Запускаем нагрузочный тест [(JMeter)](otus_highload_test_plan.jmx)
![after.png](after_replica%2Fafter.png)
![after_1.png](after_replica%2Fafter_1.png)
![after_2.png](after_replica%2Fafter_2.png)
![after_3.png](after_replica%2Fafter_3.png)
При переносе запросов на слейв нагрузка на мастер снизилась

### Настроить два слейва и один мастер (Описание сокращенно, без точных команд).
1) Сделал дополнительную копию слейва `db-slave-2` (по аналогии `db-slave`)
2) Запустил программу с флагом генерации тестовых данных. [(JMeter)](otus_highload_test_plan.jmx)
3) Завершил работы мастера `db-master` командой `docker stop db-master`
4) Запромоутил `db-slave-2`. Pg_promote()
5) Для реплики `db-slave-2` изменил настройку чтобы мастер стал `db-slave`
6) Проверил количество записей в базе `db-master`: 1002174 в базах `db-slave` и `db-slave-2`: 1001849
