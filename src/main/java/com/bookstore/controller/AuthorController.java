package com.bookstore.controller;

import com.bookstore.dto.author.AuthorDto;
import com.bookstore.dto.author.AuthorRequestDto;
import com.bookstore.dto.author.AuthorUpdateRequestDto;
import com.bookstore.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Validated
@RequestMapping("/v1/authors")
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    public Page<AuthorDto> findAllAuthors(@ParameterObject Pageable pageable) {
        return authorService.findAll(pageable);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<AuthorDto> createAuthor(@Valid @RequestBody AuthorRequestDto authorRequest) {
        AuthorDto savedAuthor = authorService.create(authorRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedAuthor.getId()).toUri();
        return ResponseEntity.created(location).body(savedAuthor);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
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


    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public void deleteAuthor(@PathVariable(name = "id") Long id) {
        authorService.delete(id);
    }
}
