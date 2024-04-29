CREATE SCHEMA IF NOT EXISTS chat;

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