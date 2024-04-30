# highload

### Инструкция по локальному запуску приложения

Перед запуском приложения необходимо установить следующее программное обеспечение:

1. Postman `https://www.postman.com/downloads/`
2. Docker `https://www.docker.com/products/docker-desktop/`

### Запуск приложения

1. Выполните команду `docker-compose up -d`

### Реализованные endpoint:

* /login
* /user/register
* /user/get/{id}
* /user/search
* /friend/set/{user_id}
* /friend/delete/{user_id}
* /post/create
* /post/update
* /post/delete/{id}
* /post/get/{id}
* /post/feed
* /dialog/{user_id}/send
* /dialog/{user_id}/list

### Postman-коллекция

Postman-коллекция располагается в корне проекта [Postman-коллекция](./postman_collection.json)