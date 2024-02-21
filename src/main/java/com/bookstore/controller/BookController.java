package com.bookstore.controller;

import com.bookstore.controller.filters.BookFilter;
import com.bookstore.dto.book.BookDto;
import com.bookstore.dto.book.BookRequestDto;
import com.bookstore.dto.rating.BookRatingDto;
import com.bookstore.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1/books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    public Page<BookDto> findBooksPaged(BookFilter bookFilter, Pageable pageable) {
        return bookService.findAll(bookFilter, pageable);
    }

    @GetMapping("/{isbn}")
    public BookDto findBookByIsbn(@PathVariable(name = "isbn") String isbn) {
        return bookService.findByIsbn(isbn);
    }

    @GetMapping("/{isbn}/ratings")
    public Page<BookRatingDto> getBookRatings(@PathVariable(name = "isbn") String isbn, Pageable pageable) {
        return bookService.findRatingsByBookIsbn(isbn, pageable);
    }

    @PostMapping
    public BookDto createBook(@Valid @RequestBody BookRequestDto bookRequest) {
        return bookService.create(bookRequest);
    }

    @PutMapping("/{isbn}")
    public BookDto updateBook(@PathVariable(name = "isbn") String isbn, @Valid @RequestBody BookRequestDto bookRequest) {
        if (!isbn.equals(bookRequest.getIsbn())) {
            throw new RuntimeException("ISBN in path should be equal to ISBN in request body!");
        }
        return bookService.update(isbn, bookRequest);
    }

    @DeleteMapping("/{isbn}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable(name = "isbn") String isbn) {
        bookService.delete(isbn);
    }
}
