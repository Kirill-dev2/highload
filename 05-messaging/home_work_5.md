# Очереди и отложенное выполнение

### Конфигурация RabbitMQ 
1. Подключаемся к docker контейнеру: `docker exec -it rabbitmq bash`
2. Устанавливаем плагин: `rabbitmq-plugins enable rabbitmq_stomp`

### Описание
Добавлена возможность в режиме реального времени публиковать новые посты через вебсокет.
Пользователь должен быть залогинен и в вебсокет запросе предоставить токен. 
Stomp server делегирован в message broker rabbitMQ, что позволяет автоматически создавать queue и exchange. 

При публикации нового поста, получаем всех друзей пользователя и на каждого друга создаем очередь в формате subscription.
Биндинг происходить по id пользователя, если к этому времени он был подключен через веб-сокет то ему тут же отправится новый пост. 

Благодаря тому что очереди сделаны exclusive'ными сервис линейно масштабируется - при подключении нового сервиса соединение с очередью rabbit'а получит только один из инстансов и публиковать в вебсокет будет только один инстанс, через который произошел логин.

Для проверки корректной работы сервиса вебсокетов, был реализован простой SockJs client [index.html](index.html)

### Сценарий:
* Залогиниться тремя разными пользователями и подключиться с полученными токенами к вебсокету
* Проверить что пользователь №1 и №2 друзья, а №3 не является другом пользователя №1 
* Опубликовать пост от пользователь №1
* Проверить что в вебсокет пользователя №2 прилетел новый пост
* Если опубликовать пост от пользователя №3 то пользователи №1, №2 не получили пост

### Масштабирование RabbitMQ
1. docker run -itd -p 5672:5672 -p 15672:15672 --name rabbit rabbitmq:3.13.1-management
2. docker exec -it rabbit bash
3. docker network create cluster-network
4. docker run -d --hostname node1.rabbit --net cluster-network --name rabbitNode1 --add-host node2.rabbit:172.24.0.3 -p  "15673:15672" -e "RABBITMQ_USE_LONGNAME=true" -e RABBITMQ_ERLANG_COOKIE="cookie" rabbitmq:3-management
5. docker run -d --hostname node2.rabbit --net cluster-network --name rabbitNode2 --add-host node1.rabbit:172.24.0.2 -p  "15674:15672" -e "RABBITMQ_USE_LONGNAME=true" -e RABBITMQ_ERLANG_COOKIE="cookie" rabbitmq:3-management
6. docker exec -it rabbitNode1 bash
7. rabbitmqctl stop_app
8. rabbitmqctl join_cluster rabbit@node2.rabbit
9. rabbitmqctl start_app