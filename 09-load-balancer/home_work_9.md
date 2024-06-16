# Балансировка и отказоустойчивость
1) Два инстанса PostgreSQL
```
db-slave-1:
image: postgres:16-alpine
container_name: db-slave-1
volumes:
- slave:/var/lib/postgresql/data
environment:
POSTGRES_USER: postgres
POSTGRES_PASSWORD: mysecretpassword
ports:
- "5433:5432"
depends_on:
- db-master
```
```
db-slave-2:
image: postgres:16-alpine
container_name: db-slave-2
volumes:
- slave-2:/var/lib/postgresql/data
environment:
POSTGRES_USER: postgres
POSTGRES_PASSWORD: mysecretpassword
ports:
- "5434:5432"
depends_on:
- db-master
```
2) Два инстанса бэк
```
  core-service-1:
    image: highload-core:1.0.1
    container_name: core-1
    build:
      context: .
      dockerfile: core/Dockerfile
    ports:
      - "8082:8082"
    depends_on:
      - db-master
      - db-slave-1
      - db-slave-2
      - redis
      - rabbitmq
      - chat-service
    environment:
      - SERVER_PORT=8082
      - SPRING_PROFILES_ACTIVE=default
      - DB_MASTER_URL=jdbc:postgresql://db-master/postgres
      - DB_SLAVE_URL=jdbc:postgresql://haproxy:5488/postgres
      - REDIS_HOST=redis-cache
      - REDIS_PORT=6379
      - RABBITMQ_HOST=rabbitmq
      - RABBITMQ_USERNAME=guest
      - RABBITMQ_PASSWORD=guest
      - RABBITMQ_PORT=61613
      - DIALOG_SERVICE=chat-service:8081
```
```
  core-service-2:
    image: highload-core:1.0.1
    container_name: core-2
    build:
      context: .
      dockerfile: core/Dockerfile
    ports:
      - "8083:8083"
    depends_on:
      - db-master
      - db-slave-1
      - db-slave-2
      - redis
      - rabbitmq
      - chat-service
    environment:
      - SERVER_PORT=8083
      - SPRING_PROFILES_ACTIVE=default
      - DB_MASTER_URL=jdbc:postgresql://db-master/postgres
      - DB_SLAVE_URL=jdbc:postgresql://haproxy:5488/postgres
      - REDIS_HOST=redis-cache
      - REDIS_PORT=6379
      - RABBITMQ_HOST=rabbitmq
      - RABBITMQ_USERNAME=guest
      - RABBITMQ_PASSWORD=guest
      - RABBITMQ_PORT=61613
      - DIALOG_SERVICE=chat-service:8081
```

### Конфигурация [haproxy](haproxy/haproxy.cfg)

```
bind *:5488 - IP-адрес и порты, к которым могут подключаться клиенты.
balance roundrobin - метод балансировки
server db-slave db-slave:5432 check - первый слейв PostgreSQL.
server db-slave-2 db-slave-2:5432 check - второй слейв PostgreSQL.
```

### Конфигурация [nginx](nginx/nginx.conf)

```
upstream - module используется для определения групп серверов, на которые может ссылаться proxy_pass
${CORE_SERVICE_1} ${CORE_SERVICE_2} - Два инстанса бэк приложения
listen 8080; Порт
proxy_next_upstream     error timeout invalid_header http_500;
proxy_connect_timeout   1;  таймаут для установления соединения с прокси-сервером 
```

### Проверка работоспособности системы
1) Запускаем нагрузочный тест [Jmeter](../02-index/jmeter/otus_highload_search.jmx)
2) Выполняем команду `docker stop core-2`
3) Выполняем команду `docker stop db-slave-2`


### [Логи](logs.log)
1) В начале логов видно запросы идут на два инстанса бэк
```
nginx              | 192.168.192.1 - - [19/Jun/2024:19:23:10 +0000] "GET /user/search?first_name=%D0%9B&last_name=%D0%97 HTTP/1.1" 200 120757 "-" "Apache-HttpClient/4.5.14 (Java/21.0.2)" "-"
core-2             | 2024-06-19T19:23:10.057Z  INFO 1 --- [core] [nio-8083-exec-3] o.o.h.core.controller.UserController     : start search by param Д Н
core-1             | 2024-06-19T19:23:10.143Z  INFO 1 --- [core] [nio-8082-exec-4] o.o.h.core.controller.UserController     : start search by param Л Е
nginx              | 192.168.192.1 - - [19/Jun/2024:19:23:10 +0000] "GET /user/search?first_name=%D0%9B&last_name=%D0%95 HTTP/1.1" 200 128965 "-" "Apache-HttpClient/4.5.14 (Java/21.0.2)" "-"
core-2             | 2024-06-19T19:23:10.178Z  INFO 1 --- [core] [nio-8083-exec-4] o.o.h.core.controller.UserController     : start search by param Л П
core-1             | 2024-06-19T19:23:10.228Z  INFO 1 --- [core] [nio-8082-exec-5] o.o.h.core.controller.UserController     : start search by param Б Т
nginx              | 192.168.192.1 - - [19/Jun/2024:19:23:10 +0000] "GET /user/search?first_name=%D0%91&last_name=%D0%A2 HTTP/1.1" 200 24853 "-" "Apache-HttpClient/4.5.14 (Java/21.0.2)" "-"
nginx              | 192.168.192.1 - - [19/Jun/2024:19:23:10 +0000] "GET /user/search?first_name=%D0%94&last_name=%D0%9D HTTP/1.1" 200 631823 "-" "Apache-HttpClient/4.5.14 (Java/21.0.2)" "-"
core-2             | 2024-06-19T19:23:10.291Z  INFO 1 --- [core] [nio-8083-exec-5] o.o.h.core.controller.UserController     : start search by param Е П
nginx              | 192.168.192.1 - - [19/Jun/2024:19:23:10 +0000] "GET /user/search?first_name=%D0%9B&last_name=%D0%9F HTTP/1.1" 200 296303 "-" "Apache-HttpClient/4.5.14 (Java/21.0.2)" "-"
```

2) Далее выключается  1 slave-db

```
db-slave-2         | 2024-06-19 19:23:20.975 UTC [1] LOG:  received fast shutdown request
db-slave-2         | 2024-06-19 19:23:20.977 UTC [1] LOG:  aborting any active transactions
db-slave-2         | 2024-06-19 19:23:20.977 UTC [30] FATAL:  terminating connection due to administrator command
db-slave-2         | 2024-06-19 19:23:20.977 UTC [27] FATAL:  terminating connection due to administrator command
db-slave-2         | 2024-06-19 19:23:20.977 UTC [28] FATAL:  terminating connection due to administrator command
db-slave-2         | 2024-06-19 19:23:20.977 UTC [29] FATAL:  terminating connection due to administrator command
db-slave-2         | 2024-06-19 19:23:20.978 UTC [26] FATAL:  terminating connection due to administrator command
db-slave-2         | 2024-06-19 19:23:20.979 UTC [25] FATAL:  terminating walreceiver process due to administrator command
db-slave-2         | 2024-06-19 19:23:20.986 UTC [22] LOG:  shutting down
db-slave-2         | 2024-06-19 19:23:20.986 UTC [22] LOG:  restartpoint starting: shutdown immediate
db-slave-2         | 2024-06-19 19:23:21.007 UTC [22] LOG:  restartpoint complete: wrote 3 buffers (0.0%); 0 WAL file(s) added, 0 removed, 0 recycled; write=0.008 s, sync=0.005 s, total=0.022 s; sync files=3, longest=0.003 s, averag
e=0.002 s; distance=5 kB, estimate=5 kB; lsn=0/423F2480, redo lsn=0/423F2480
db-slave-2         | 2024-06-19 19:23:21.007 UTC [22] LOG:  recovery restart point at 0/423F2480
db-slave-2         | 2024-06-19 19:23:21.007 UTC [22] DETAIL:  Last completed transaction was at log time 2024-06-19 19:23:00.896255+00.
db-slave-2         | 2024-06-19 19:23:21.017 UTC [1] LOG:  database system is shut down
core-2             | 2024-06-19T19:23:21.019Z  INFO 1 --- [core] [nio-8083-exec-6] o.o.h.core.controller.UserController     : start search by param Г В
nginx              | 192.168.192.1 - - [19/Jun/2024:19:23:21 +0000] "GET /user/search?first_name=%D0%93&last_name=%D0%92 HTTP/1.1" 200 194469 "-" "Apache-HttpClient/4.5.14 (Java/21.0.2)" "-"
```

3) Далее выключается  1 бэк

```
nginx              | 2024/06/19 19:23:26 [error] 41#41: *16 connect() failed (111: Connection refused) while connecting to upstream, client: 192.168.192.1, server: , request: "GET /user/search?first_name=%D0%95&last_name=%D0%90 HTTP
/1.1", upstream: "http://192.168.192.14:8083/user/search?first_name=%D0%95&last_name=%D0%90", host: "localhost:8080"
nginx              | 2024/06/19 19:23:26 [warn] 41#41: *16 upstream server temporarily disabled while connecting to upstream, client: 192.168.192.1, server: , request: "GET /user/search?first_name=%D0%95&last_name=%D0%90 HTTP/1.1",
upstream: "http://192.168.192.14:8083/user/search?first_name=%D0%95&last_name=%D0%90", host: "localhost:8080"
core-1             | 2024-06-19T19:23:26.807Z  INFO 1 --- [core] [nio-8082-exec-1] o.o.h.core.controller.UserController     : start search by param М Т
core-1             | 2024-06-19T19:23:26.807Z  INFO 1 --- [core] [nio-8082-exec-4] o.o.h.core.controller.UserController     : start search by param И С
core-1             | 2024-06-19T19:23:26.807Z  INFO 1 --- [core] [nio-8082-exec-8] o.o.h.core.controller.UserController     : start search by param Е А
nginx              | 192.168.192.1 - - [19/Jun/2024:19:23:26 +0000] "GET /user/search?first_name=%D0%95&last_name=%D0%90 HTTP/1.1" 200 852445 "-" "Apache-HttpClient/4.5.14 (Java/21.0.2)" "-"
```

4) Далее видно, что система осталась работоспособной все запросы идут на 1 иснтанс бэка
```
core-1             | 2024-06-19T19:23:34.668Z  INFO 1 --- [core] [io-8082-exec-10] o.o.h.core.controller.UserController     : start search by param Ж П
nginx              | 192.168.192.1 - - [19/Jun/2024:19:23:34 +0000] "GET /user/search?first_name=%D0%96&last_name=%D0%9F HTTP/1.1" 200 12 "-" "Apache-HttpClient/4.5.14 (Java/21.0.2)" "-"
core-1             | 2024-06-19T19:23:34.802Z  INFO 1 --- [core] [nio-8082-exec-9] o.o.h.core.controller.UserController     : start search by param Д М
core-1             | 2024-06-19T19:23:34.802Z  INFO 1 --- [core] [nio-8082-exec-1] o.o.h.core.controller.UserController     : start search by param З Р
core-1             | 2024-06-19T19:23:34.802Z  INFO 1 --- [core] [nio-8082-exec-6] o.o.h.core.controller.UserController     : start search by param Н Е
```