DROP TABLE IF EXISTS books CASCADE;
DROP TABLE IF EXISTS authors CASCADE;
DROP TABLE IF EXISTS publishers CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS ratings CASCADE;
DROP TABLE IF EXISTS address CASCADE;

CREATE TABLE publishers
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE authors
(
    id   BIGSERIAL PRIMARY KEY,
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
    id          BIGSERIAL PRIMARY KEY,
    external_id BIGINT UNIQUE,
    age         INT
);

CREATE TABLE address
(
    user_id BIGSERIAL PRIMARY KEY,
    city    VARCHAR(255),
    region  VARCHAR(255),
    country VARCHAR(255)
);

CREATE TABLE ratings
(
    user_id   BIGINT REFERENCES users (id) ON DELETE CASCADE,
    book_isbn VARCHAR(50) REFERENCES books (isbn) ON DELETE CASCADE,
    score     INT NOT NULL,
    CHECK ( score BETWEEN 0 AND 10),
    PRIMARY KEY (user_id, book_isbn)
);