package com.bookstore.service;

import com.bookstore.controller.filters.BookFilter;
import com.bookstore.dto.book.BookDto;
import com.bookstore.dto.book.BookRequestDto;
import com.bookstore.dto.rating.BookRatingDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    Page<BookDto> findAll(BookFilter bookFilter, Pageable pageable);

    BookDto findByIsbn(String isbn);

    Page<BookRatingDto> findRatingsByBookIsbn(String isbn, Pageable pageable);

    BookDto create(BookRequestDto bookRequestDto);

    BookDto update(String isbn, BookRequestDto bookRequestDto);

    void delete(String isbn);
}
