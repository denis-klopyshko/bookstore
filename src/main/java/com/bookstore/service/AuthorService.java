package com.bookstore.service;

import com.bookstore.dto.author.AuthorDto;
import com.bookstore.dto.author.AuthorRequestDto;
import com.bookstore.dto.author.AuthorUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthorService {
    Page<AuthorDto> findAll(Pageable pageable);

    AuthorDto findById(Long id);

    AuthorDto create(AuthorRequestDto authorRequestDto);

    AuthorDto update(Long authorId, AuthorUpdateRequestDto updateRequest);

    void delete(Long authorId);
}
