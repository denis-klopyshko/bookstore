package com.bookstore.service.impl;

import com.bookstore.entity.*;
import com.bookstore.exception.CsvFileException;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.get;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDataUploadService {
    private static final int BATCH_SIZE = 1000;

    private final JdbcTemplate jdbcTemplate;

    private static final String[] BOOKS_CSV_HEADERS = {"ISBN", "Book-Title", "Book-Author", "Year-Of-Publication", "Publisher"};
    private static final String[] USERS_CSV_HEADERS = {"User-ID", "Location", "Age"};
    private static final String[] RATINGS_CSV_HEADERS = {"User-ID", "ISBN", "Book-Rating"};

    @Transactional
    public void processBooksFile(MultipartFile file) {
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
            log.error("There was an error processing books: {}", ex.getMessage());
            throw new CsvFileException("Error parsing csv file", ex);
        }
    }

    @Transactional
    public void processUsersFile(MultipartFile file) {
        CSVFormat format = this.getCsvFormat(USERS_CSV_HEADERS);
        try (final CSVParser records = CSVParser.parse(file.getInputStream(), StandardCharsets.UTF_8, format)) {
            Set<User> users = new HashSet<>();

            records.stream().sequential().forEach(line -> {
                var userExternalId = Long.valueOf(line.get(0));
                var userAge = Optional.ofNullable(line.get(2))
                        .filter(v -> !v.equals("NULL"))
                        .map(Integer::valueOf)
                        .orElse(null);
                var user = User.builder().age(userAge).externalId(userExternalId).build();

                var addressSplitted = Arrays.stream(line.get(1).split(",", -1))
                        .map(this::sanitizeString)
                        .filter(str -> str.length() > 0)
                        .toList();
                var address = Address.builder()
                        .city(get(addressSplitted, 0, null))
                        .region(get(addressSplitted, 1, null))
                        .country(get(addressSplitted, 2, null))
                        .build();
                user.setAddress(address);
                users.add(user);
            });

            saveUsers(users);
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

            records.stream().sequential().forEach(record -> {
                var userExternalId = Long.valueOf(record.get(0));
                var bookIsbn = sanitizeString(record.get(1));
                var ratingScore = Integer.valueOf(record.get(2));

                // User Rating can't be 0. Not valid data. Skipping...
                if (ratingScore.equals(0)) {
                    return;
                }

                var ratingPk = new Rating.BookRatingPrimaryKey(userExternalId, bookIsbn);
                var rating = Rating.builder().id(ratingPk).score(ratingScore).build();
                ratings.add(rating);
            });

            saveRatings(ratings);
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
                .map(Rating::getId)
                .map(Rating.BookRatingPrimaryKey::getBookIsbn)
                .map(StringUtils::quote)
                .collect(Collectors.joining(","));

        var userExternalIds = chunk.stream()
                .map(Rating::getId)
                .map(Rating.BookRatingPrimaryKey::getUserId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        var booksSql = String.format("SELECT b.* FROM books b WHERE b.isbn IN (%s)", bookIsbns);
        Map<String, Book> bookMap = jdbcTemplate.query(booksSql, BeanPropertyRowMapper.newInstance(Book.class))
                .stream()
                .collect(toMap(Book::getIsbn, Function.identity()));

        var usersSql = String.format("SELECT u.* FROM users u WHERE u.external_id IN (%s)", userExternalIds);
        Map<Long, User> usersMap = jdbcTemplate.query(usersSql, BeanPropertyRowMapper.newInstance(User.class))
                .stream()
                .collect(toMap(User::getExternalId, Function.identity()));

        var newRatingsChunk = chunk.stream()
                .map(rating -> createNewRating(rating, bookMap, usersMap))
                .filter(Objects::nonNull)
                .toList();

        var insertRatingSql = "INSERT INTO ratings(user_id, book_isbn, score) VALUES (?, ?, ?)";
        jdbcTemplate.batchUpdate(insertRatingSql, newRatingsChunk, newRatingsChunk.size(), (ps, rating) -> {
            ps.setLong(1, rating.getUser().getId());
            ps.setString(2, rating.getBook().getIsbn());
            ps.setDouble(3, rating.getScore());
        });
    }

    private Rating createNewRating(Rating rating, Map<String, Book> booksMap, Map<Long, User> usersMap) {
        var book = booksMap.get(rating.getId().getBookIsbn());
        var user = usersMap.get(rating.getId().getUserId());

        if (book == null || user == null) {
            return null;
        }

        return Rating.builder()
                .user(user)
                .book(book)
                .score(rating.getScore())
                .build();
    }

    private void saveUsers(Set<User> users) {
        Iterable<List<User>> usersChunks = Iterables.partition(users, BATCH_SIZE);
        usersChunks.forEach(this::saveUsersChunk);
    }

    private void saveUsersChunk(List<User> usersChunk) {
        var insertUserSql = "INSERT INTO users(id, external_id, age) VALUES (nextval('users_id_seq'), ?, ?)";
        jdbcTemplate.batchUpdate(insertUserSql, usersChunk, usersChunk.size(), (ps, user) -> {
            ps.setLong(1, user.getExternalId());
            ps.setObject(2, user.getAge());
        });

        var userIdSubQuery = "SELECT u.id from users u where u.external_id = ?";
        var insertAddressSql = String.format(
                "INSERT INTO address(user_id, city, region, country) VALUES ((%s), ?, ?, ?)",
                userIdSubQuery
        );
        jdbcTemplate.batchUpdate(insertAddressSql, usersChunk, usersChunk.size(), (ps, user) -> {
            ps.setLong(1, user.getExternalId());
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
        var insertAuthorSql = "INSERT INTO authors(id, name) VALUES (nextval('authors_id_seq'), ?)";
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
        var insertPublisherSql = "INSERT INTO publishers(id, name) VALUES (nextval('publishers_id_seq'), ?)";
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
                        " VALUES (nextval('books_id_seq'), ?, ?, (%s), (%s), ?)",
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
                .trimFrom(strToSanitize)
                .trim();
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
