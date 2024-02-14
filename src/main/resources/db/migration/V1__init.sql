DROP TABLE IF EXISTS books CASCADE;
DROP TABLE IF EXISTS authors CASCADE;
DROP TABLE IF EXISTS publishers CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS ratings CASCADE;
DROP TABLE IF EXISTS address CASCADE;
DROP SEQUENCE IF EXISTS publisher_entity_seq;
DROP SEQUENCE IF EXISTS author_entity_seq;
DROP SEQUENCE IF EXISTS book_entity_seq;

CREATE SEQUENCE publisher_entity_seq START 1 INCREMENT 1;
CREATE SEQUENCE author_entity_seq START 1 INCREMENT 1;
CREATE SEQUENCE book_entity_seq START 1 INCREMENT 1;

CREATE TABLE publishers
(
    id   BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE authors
(
    id   BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE books
(
    id           BIGSERIAL PRIMARY KEY,
    isbn         VARCHAR(50)  NOT NULL UNIQUE,
    title        VARCHAR(255) NOT NULL,
    publisher_id BIGINT       NOT NULL REFERENCES publishers (id),
    author_id    BIGINT       NOT NULL REFERENCES authors (id),
    year         INT          NOT NULL
);

CREATE TABLE users
(
    id  BIGSERIAL PRIMARY KEY,
    age INT
);

CREATE TABLE address
(
    user_id BIGSERIAL PRIMARY KEY,
    city    VARCHAR(255) NOT NULL,
    region  VARCHAR(255),
    country VARCHAR(255)
);

CREATE TABLE ratings
(
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    book_isbn VARCHAR(50) REFERENCES books (isbn) ON DELETE CASCADE,
    rating    INT    NOT NULL,
    CHECK ( rating BETWEEN 0 AND 10)
);