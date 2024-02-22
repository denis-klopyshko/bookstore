package com.bookstore.service.impl;

import com.bookstore.controller.filters.BookFilter;
import com.bookstore.dto.book.BookDto;
import com.bookstore.dto.book.BookRequestDto;
import com.bookstore.dto.rating.BookRatingDto;
import com.bookstore.entity.Author;
import com.bookstore.entity.Book;
import com.bookstore.entity.Publisher;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapping.BookMapper;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.PublisherRepository;
import com.bookstore.repository.RatingRepository;
import com.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class BookServiceImpl implements BookService {
    private static final BookMapper MAPPER = BookMapper.INSTANCE;
    private final BookRepository bookRepo;
    private final RatingRepository ratingRepo;
    private final AuthorRepository authorRepo;
    private final PublisherRepository publisherRepo;

    @Transactional(readOnly = true)
    @Override
    public Page<BookDto> findAll(BookFilter bookFilter, Pageable pageable) {
        Page<BookDto> booksPage = bookRepo
                .findAll(bookFilter.toSpec(), pageable)
                .map(MAPPER::mapToDto);

        List<String> bookIsbns = booksPage.map(BookDto::getIsbn).toList();
        Map<String, Double> bookRatings = ratingRepo.findAverageRatingByBookIsbnsIn(bookIsbns)
                .stream().collect(Collectors.toMap(
                        arr -> (String) arr[0],   // Book ISBN
                        arr -> (Double) arr[1]    // Average
                ));

        return booksPage.map(book -> {
            var avgRating = bookRatings.getOrDefault(book.getIsbn(), 0.0);
            book.setRating(avgRating);
            return book;
        });
    }

    @Transactional(readOnly = true)
    @Override
    public BookDto findByIsbn(String isbn) {
        var book = findBookEntity(isbn);
        return mapToResponse(book);
    }

    @Override
    public Page<BookRatingDto> findRatingsByBookIsbn(String isbn, Pageable pageable) {
        return ratingRepo.findAllByBookIsbn(isbn, pageable)
                .map(rating -> new BookRatingDto(rating.getUser().getId(), rating.getScore()));
    }

    @Override
    public BookDto create(BookRequestDto bookRequestDto) {
        validateBookExist(bookRequestDto.getIsbn());
        var author = findAuthorEntity(bookRequestDto.getAuthor().getId());
        var publisher = findPublisherEntity(bookRequestDto.getPublisher().getId());
        var book = MAPPER.mapToEntity(bookRequestDto);
        book.setAuthor(author);
        book.setPublisher(publisher);
        return mapToResponse(bookRepo.save(book));
    }

    @Override
    public BookDto update(String isbn, BookRequestDto bookRequestDto) {
        var bookEntity = findBookEntity(isbn);
        bookEntity.setTitle(bookRequestDto.getTitle());
        bookEntity.setYear(bookRequestDto.getYear());

        if (!bookEntity.getPublisher().getId().equals(bookRequestDto.getPublisher().getId())) {
            var publisher = findPublisherEntity(bookRequestDto.getPublisher().getId());
            bookEntity.setPublisher(publisher);
        }

        if (!bookEntity.getAuthor().getId().equals(bookRequestDto.getAuthor().getId())) {
            var author = findAuthorEntity(bookRequestDto.getAuthor().getId());
            bookEntity.setAuthor(author);
        }

        return mapToResponse(bookRepo.save(bookEntity));
    }

    @Override
    public void delete(String isbn) {
        var book = findBookEntity(isbn);
        bookRepo.delete(book);
    }

    private Book findBookEntity(String isbn) {
        return bookRepo.findByIsbn(isbn).orElseThrow(() -> {
            log.warn("Book not found by isbn: [{}]", isbn);
            return new ResourceNotFoundException(String.format("Book not found by isbn: %s", isbn));
        });
    }

    private Author findAuthorEntity(Long authorId) {
        return authorRepo.findById(authorId).orElseThrow(() -> {
            log.warn("Author not found by id: [{}]", authorId);
            return new ResourceNotFoundException(String.format("Author not found by id: %d", authorId));
        });
    }

    private Publisher findPublisherEntity(Long publisherId) {
        return publisherRepo.findById(publisherId).orElseThrow(() -> {
            log.warn("Publisher not found by id: [{}]", publisherId);
            return new ResourceNotFoundException(String.format("Publisher not found by id: %d", publisherId));
        });
    }

    private void validateBookExist(String isbn) {
        if (bookRepo.existsByIsbn(isbn)) {
            throw new ConflictException(String.format("Book with isbn [%s] already exists!", isbn));
        }
    }

    private BookDto mapToResponse(Book book) {
        var bookDto = MAPPER.mapToDto(book);
        var avgRating = bookRepo.calculateAverageRating(book).orElse(0.0);
        bookDto.setRating(avgRating);
        return bookDto;
    }
}
