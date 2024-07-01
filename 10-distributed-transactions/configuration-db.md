# Распределенные транзакции

### Настройка на мастере

1) Подключаемся к docker контейнеру: `docker exec -it db-counter-master bash`
2) Заходим под пользователем postgres: `su - postgres`
3) Создаем пользователя, под которым будем подключаться со стороны вторичного сервера: `createuser --replication -P repluser`
4) Открываем конфигурационный файл _postgresql.conf_: `vi /var/lib/postgresql/data/postgresql.conf`
Приводим к следующием виду некоторые параметры:

```
wal_level = replica
max_wal_senders = 4
max_replication_slots = 4
wal_keep_size = 32
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
    2) Добавляем запись: `host    replication     replicator       192.168.192.0/20          md5` или `host    replication     all             192.168.192.0/20        trust`
    3) Перезапускаем мастер: `docker restart db-master`

### Настройка на слейве

1) Подключаемся к docker контейнеру: `docker exec -it db-counter-slave bash`
2) удалим содержимое рабочего каталога вторичной базы: `rm -r /var/lib/postgresql/data/*`
3) Выполняем команду: 
 ```
pg_basebackup --host=db-counter-master --username=repluser --pgdata=/var/lib/postgresql/data --wal-method=stream --write-recovery-conf 
 ```
 ```
* db-maste — мастер; /var/lib/postgresql/data — путь до каталога с данными слейва.