package com.bookstore.controller;

import com.bookstore.dto.author.AuthorDto;
import com.bookstore.dto.author.AuthorRequestDto;
import com.bookstore.dto.author.AuthorUpdateRequestDto;
import com.bookstore.service.impl.AuthorServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Validated
@RequestMapping("/v1/authors")
public class AuthorController {

    private final AuthorServiceImpl authorService;

    @GetMapping
    public Page<AuthorDto> findAllAuthors(Pageable pageable) {
        return authorService.findAll(pageable);
    }

    @PostMapping
    public ResponseEntity<AuthorDto> createAuthor(@Valid @RequestBody AuthorRequestDto authorRequest) {
        AuthorDto savedAuthor = authorService.create(authorRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedAuthor.getId()).toUri();
        return ResponseEntity.created(location).body(savedAuthor);
    }

    @PutMapping("/{id}")
    public AuthorDto updateAuthor(@PathVariable(name = "id") Long id,
                                  @Valid @RequestBody AuthorUpdateRequestDto updateRequest) {
        if (!id.equals(updateRequest.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id in path should be equal to id in request body!");
        }

        return authorService.update(id, updateRequest);
    }

    @GetMapping("/{id}")
    public AuthorDto findAuthor(@PathVariable(name = "id") Long id) {
        return authorService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteAuthor(@PathVariable(name = "id") Long id) {
        authorService.delete(id);
    }
}
