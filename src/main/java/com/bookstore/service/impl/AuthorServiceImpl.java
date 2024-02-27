package com.bookstore.service.impl;

import com.bookstore.dto.author.AuthorDto;
import com.bookstore.dto.author.AuthorRequestDto;
import com.bookstore.dto.author.AuthorUpdateRequestDto;
import com.bookstore.entity.Author;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapping.AuthorMapper;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.service.AuthorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class AuthorServiceImpl implements AuthorService {
    private static final AuthorMapper MAPPER = AuthorMapper.INSTANCE;
    private final AuthorRepository authorRepo;

    @Transactional(readOnly = true)
    @Override
    public Page<AuthorDto> findAll(Pageable pageable) {
        return authorRepo.findAll(pageable)
                .map(MAPPER::mapToDto);
    }

    @Transactional(readOnly = true)
    @Override
    public AuthorDto findById(Long authorId) {
        var author = findAuthorEntity(authorId);
        return MAPPER.mapToDto(author);
    }

    @Override
    public AuthorDto create(AuthorRequestDto authorRequestDto) {
        var authorEntity = MAPPER.mapToEntity(authorRequestDto);
        validateAuthorExist(authorRequestDto.getName());
        var savedAuthor = authorRepo.save(authorEntity);
        return MAPPER.mapToDto(savedAuthor);
    }

    @Override
    public AuthorDto update(Long authorId, AuthorUpdateRequestDto authorUpdateRequest) {
        validateAuthorExist(authorUpdateRequest.getName());
        var authorEntity = findAuthorEntity(authorId);
        if (!authorEntity.getName().equals(authorUpdateRequest.getName())) {
            validateAuthorExist(authorUpdateRequest.getName());
        }
        authorEntity.setName(authorUpdateRequest.getName());
        return MAPPER.mapToDto(authorRepo.save(authorEntity));

    }

    @Override
    public void delete(Long authorId) {
        var author = findAuthorEntity(authorId);
        if (!author.getBooks().isEmpty()) {
            throw new ConflictException(
                    String.format("Can't delete author: [%d]. Books not empty!", authorId)
            );
        }

        authorRepo.delete(author);
    }

    private Author findAuthorEntity(Long authorId) {
        return authorRepo.findById(authorId).orElseThrow(
                () -> {
                    log.warn("Author not found by id: [{}]", authorId);
                    return new ResourceNotFoundException("Author not found by id: " + authorId);
                }
        );
    }

    private void validateAuthorExist(String authorName) {
        if (authorRepo.existsByName(authorName)) {
            throw new ConflictException(String.format("Author with name [%s] already exists.", authorName));
        }
    }
}
