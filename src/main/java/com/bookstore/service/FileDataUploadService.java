package com.bookstore.service;

import com.bookstore.entity.*;
import com.bookstore.exception.CsvFileException;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Iterables;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class FileDataUploadService {
    private static final int BATCH_SIZE = 1000;

    private final JdbcTemplate jdbcTemplate;

    private static final String[] BOOKS_CSV_HEADERS = {"ISBN", "Book-Title", "Book-Author", "Year-Of-Publication", "Publisher"};
    private static final String[] USERS_CSV_HEADERS = {"User-ID", "Location", "Age"};
    private static final String[] RATINGS_CSV_HEADERS = {"User-ID", "ISBN", "Book-Rating"};

    @Transactional
    public void processBooksFile(@Valid MultipartFile file) {
        CSVFormat format = this.getCsvFormat(BOOKS_CSV_HEADERS);
        try (final CSVParser records = CSVParser.parse(file.getInputStream(), StandardCharsets.UTF_8, format)) {
            Set<Publisher> publishers = new HashSet<>();
            Set<Author> authors = new HashSet<>();
            Set<Book> books = new HashSet<>();

            records.stream().sequential().forEach(line -> {
                var publisher = Publisher.builder().name(line.get(4)).build();
                publishers.add(publisher);

                var bookAuthor = Author.ofName(sanitizeString(line.get(2)));
                authors.add(bookAuthor);

                var book = Book.builder()
                        .isbn(sanitizeString(line.get(0)))
                        .title(line.get(1))
                        .publisher(publisher)
                        .author(bookAuthor)
                        .year(Integer.valueOf(line.get(3)))
                        .build();
                books.add(book);
            });

            saveAuthors(authors);
            savePublishers(publishers);
            saveBooks(books);
        } catch (Exception ex) {
            log.error("Error", ex);
            log.error("There was an error processing books: {}", ex.getMessage());
            throw new CsvFileException("Error parsing csv file", ex);
        }
    }

    @SneakyThrows
    @Transactional
    public void processUsersFile(MultipartFile file) {
        CSVFormat format = this.getCsvFormat(USERS_CSV_HEADERS);
        try (final CSVParser records = CSVParser.parse(file.getInputStream(), StandardCharsets.UTF_8, format)) {
            Set<User> users = new HashSet<>();
            var start = Instant.now();

            records.stream().sequential().forEach(line -> {
                var userId = Long.valueOf(line.get(0));
                var userAge = Optional.ofNullable(line.get(2))
                        .filter(v -> !v.equals("NULL"))
                        .map(Integer::valueOf)
                        .orElse(null);
                var user = User.builder().age(userAge).id(userId).build();

                var addressSplitted = Arrays.stream(line.get(1).split(",", -1)).toList();
                var address = Address.builder()
                        .city(Iterables.get(addressSplitted, 0, null))
                        .region(Iterables.get(addressSplitted, 1, null))
                        .country(Iterables.get(addressSplitted, 2, null))
                        .build();
                user.setAddress(address);
                users.add(user);
            });

            saveUsers(users);
            var end = Instant.now();
            log.info("Finished to process users: {} seconds", Duration.between(start, end).toSeconds());
        } catch (Exception ex) {
            log.error("There was an error processing users: {}", ex.getMessage());
            throw new CsvFileException("Error parsing csv file", ex);
        }
    }

    @Transactional
    public void processRatingsFile(MultipartFile file) {
        CSVFormat format = this.getCsvFormat(RATINGS_CSV_HEADERS);
        try (final CSVParser records = CSVParser.parse(file.getInputStream(), StandardCharsets.UTF_8, format)) {
            Set<Rating> ratings = new HashSet<>();
            var start = Instant.now();

            records.stream().sequential().forEach(record -> {
                var userId = Long.valueOf(record.get(0));
                var user = User.builder().id(userId).build();

                var bookIsbn = sanitizeString(record.get(1));
                var book = Book.builder().isbn(bookIsbn).build();

                var rating = Rating.builder()
                        .user(user)
                        .book(book)
                        .rating(Integer.valueOf(record.get(2)))
                        .build();

                ratings.add(rating);
            });

            saveRatings(ratings);
            var end = Instant.now();
            log.info("Finished to process ratings: {} seconds", Duration.between(start, end).toSeconds());
        } catch (Exception ex) {
            log.error("There was an error processing ratings: {}", ex.getMessage());
            throw new CsvFileException("Error parsing csv file", ex);
        }
    }

    private void saveRatings(Set<Rating> ratings) {
        Iterable<List<Rating>> ratingsChunks = Iterables.partition(ratings, BATCH_SIZE);
        ratingsChunks.forEach(this::saveRatingsChunk);
    }

    private void saveRatingsChunk(List<Rating> chunk) {
        var bookIsbns = chunk.stream()
                .map(Rating::getBook)
                .map(book -> StringUtils.quote(book.getIsbn()))
                .collect(Collectors.joining(","));

        var userIds = chunk.stream()
                .map(Rating::getUser)
                .map(User::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        var booksSql = String.format("SELECT b.* FROM books b WHERE b.isbn IN (%s)", bookIsbns);
        Map<String, Book> bookMap = jdbcTemplate.query(booksSql, new BeanPropertyRowMapper<>(Book.class))
                .stream()
                .collect(toMap(Book::getIsbn, Function.identity()));

        var usersSql = String.format("SELECT u.* FROM users u WHERE u.id IN (%s)", userIds);
        Map<Long, User> usersMap = jdbcTemplate.query(usersSql, new BeanPropertyRowMapper<>(User.class))
                .stream()
                .collect(toMap(User::getId, Function.identity()));

        var newRatingsChunk = chunk.stream()
                .map(rating -> createNewRating(rating, bookMap, usersMap))
                .filter(Objects::nonNull)
                .toList();

        var insertRatingSql = "INSERT INTO ratings(user_id, book_isbn, rating) VALUES (?, ?, ?)";
        jdbcTemplate.batchUpdate(insertRatingSql, newRatingsChunk, newRatingsChunk.size(), (ps, rating) -> {
            ps.setLong(1, rating.getUser().getId());
            ps.setString(2, rating.getBook().getIsbn());
            ps.setInt(3, rating.getRating());
        });
    }

    private Rating createNewRating(Rating rating, Map<String, Book> booksMap, Map<Long, User> usersMap) {
        var book = booksMap.get(rating.getBook().getIsbn());
        var user = usersMap.get(rating.getUser().getId());

        if (book == null || user == null) {
            return null;
        }

        return Rating.builder()
                .user(user)
                .book(book)
                .rating(rating.getRating())
                .build();
    }

    private void saveUsers(Set<User> users) {
        Iterable<List<User>> usersChunks = Iterables.partition(users, BATCH_SIZE);
        usersChunks.forEach(this::saveUsersChunk);
    }

    private void saveUsersChunk(List<User> usersChunk) {
        var insertUserSql = "INSERT INTO users(id, age) VALUES (?, ?)";
        var insertAddressSql = "INSERT INTO address(user_id, city, region, country) VALUES (?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(insertUserSql, usersChunk, usersChunk.size(), (ps, user) -> {
            ps.setLong(1, user.getId());
            ps.setObject(2, user.getAge());
        });

        jdbcTemplate.batchUpdate(insertAddressSql, usersChunk, usersChunk.size(), (ps, user) -> {
            ps.setLong(1, user.getId());
            ps.setString(2, user.getAddress().getCity());
            ps.setString(3, user.getAddress().getRegion());
            ps.setString(4, user.getAddress().getCountry());
        });
    }

    private void saveAuthors(Set<Author> authors) {
        Iterable<List<Author>> authorsChunks = Iterables.partition(authors, BATCH_SIZE);
        authorsChunks.forEach(this::saveAuthorsChunk);
    }

    private void saveAuthorsChunk(List<Author> authorsChunk) {
        var insertAuthorSql = "INSERT INTO authors(id, name) VALUES (nextval('author_entity_seq'), ?)";
        jdbcTemplate.batchUpdate(
                insertAuthorSql,
                authorsChunk,
                authorsChunk.size(),
                (ps, author) -> ps.setString(1, author.getName()));
    }

    private void savePublishers(Set<Publisher> publishers) {
        Iterable<List<Publisher>> publisherChunks = Iterables.partition(publishers, BATCH_SIZE);
        publisherChunks.forEach(this::savePublishersChunk);
    }

    private void savePublishersChunk(List<Publisher> publishersChunk) {
        var insertPublisherSql = "INSERT INTO publishers(id, name) VALUES (nextval('publisher_entity_seq'), ?)";
        jdbcTemplate.batchUpdate(
                insertPublisherSql,
                publishersChunk,
                publishersChunk.size(),
                (ps, publisher) -> ps.setString(1, publisher.getName()));
    }

    private void saveBooks(Set<Book> books) {
        Iterable<List<Book>> booksChunks = Iterables.partition(books, BATCH_SIZE);
        booksChunks.forEach(this::saveBooksChunk);
    }

    private void saveBooksChunk(List<Book> booksChunk) {
        var authorSubQuery = "SELECT a.id FROM authors a WHERE a.name = ?";
        var publisherSubQuery = "SELECT p.id FROM publishers p WHERE p.name = ?";
        var insertBookSql = String.format(
                "INSERT INTO books(id, isbn, title, publisher_id, author_id, year)" +
                        " VALUES (nextval('book_entity_seq'), ?, ?, (%s), (%s), ?)",
                publisherSubQuery, authorSubQuery
        );
        jdbcTemplate.batchUpdate(insertBookSql, booksChunk, booksChunk.size(),
                (ps, book) -> {
                    ps.setString(1, book.getIsbn());
                    ps.setString(2, book.getTitle());
                    ps.setString(3, book.getPublisher().getName());
                    ps.setString(4, book.getAuthor().getName());
                    ps.setInt(5, book.getYear());
                });
    }

    private String sanitizeString(String strToSanitize) {
        return CharMatcher
                .is('\'')
                .trimFrom(strToSanitize);
    }

    private CSVFormat getCsvFormat(String[] headers) {
        return CSVFormat.RFC4180.builder()
                .setHeader(headers)
                .setEscape('\\')
                .setDelimiter(';')
                .setIgnoreSurroundingSpaces(true)
                .setIgnoreEmptyLines(true)
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build();
    }
}
