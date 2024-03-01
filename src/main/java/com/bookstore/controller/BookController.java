package com.bookstore.controller;

import com.bookstore.controller.filters.BookFilter;
import com.bookstore.dto.book.BookDto;
import com.bookstore.dto.book.BookRequestDto;
import com.bookstore.dto.rating.BookRatingDto;
import com.bookstore.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1/books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    public Page<BookDto> findBooksPaged(@ParameterObject BookFilter bookFilter, @ParameterObject Pageable pageable) {
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

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<BookDto> createBook(@Valid @RequestBody BookRequestDto bookRequest) {
        BookDto savedBook = bookService.create(bookRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{isbn}")
                .buildAndExpand(savedBook.getIsbn()).toUri();

        return ResponseEntity.created(location).body(savedBook);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{isbn}")
    public BookDto updateBook(@PathVariable(name = "isbn") String isbn, @Valid @RequestBody BookRequestDto bookRequest) {
        if (!isbn.equals(bookRequest.getIsbn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ISBN in path should be equal to ISBN in request body!");
        }

        return bookService.update(isbn, bookRequest);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{isbn}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable(name = "isbn") String isbn) {
        bookService.delete(isbn);
    }
}
