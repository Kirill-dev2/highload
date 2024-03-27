# highload

### Инструкция по локальному запуску приложения

Перед запуском приложения необходимо установить следующее программное обеспечение:
1. Postgres `https://www.postgresql.org/download/`, либо запустить его в docker `docker run --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=mysecretpassword -d postgres`
2. Java version 21 `https://www.oracle.com/java/technologies/downloads/`
3. Maven `https://maven.apache.org/download.cgi`
4. Postman `https://www.postman.com/downloads/`


### Запуск приложения 

1. Выполните команду `mvn clean package`
2. Выполните команду в командной строке/терминале `java -jar target/highload-0.0.1.jar`

### Реализованные endpoint:
* /login
* /user/register
* /user/get/{id}
* /user/search/

### Postman-коллекция
Postman-коллекция располагается в корне проекта [Postman-коллекция](./postman_collection.json)