package com.bookstore.controller;

import com.bookstore.dto.publisher.PublisherDto;
import com.bookstore.dto.publisher.PublisherRequestDto;
import com.bookstore.dto.publisher.PublisherUpdateRequestDto;
import com.bookstore.service.PublisherService;
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
@RequestMapping("/v1/publishers")
public class PublisherController {

    private final PublisherService publisherService;

    @GetMapping
    public Page<PublisherDto> findAllPublishers(@ParameterObject Pageable pageable) {
        return publisherService.findAll(pageable);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<PublisherDto> createPublisher(@Valid @RequestBody PublisherRequestDto publisherRequest) {
        PublisherDto savedPublisher = publisherService.create(publisherRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedPublisher.getId()).toUri();

        return ResponseEntity.created(location).body(savedPublisher);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public PublisherDto updatePublisher(@PathVariable(name = "id") Long id,
                                        @Valid @RequestBody PublisherUpdateRequestDto updateRequest) {
        if (!id.equals(updateRequest.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id in path should be equal to id in request body!");
        }

        return publisherService.update(id, updateRequest);
    }

    @GetMapping("/{id}")
    public PublisherDto findPublisher(@PathVariable(name = "id") Long id) {
        return publisherService.findById(id);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public void deletePublisher(@PathVariable(name = "id") Long id) {
        publisherService.delete(id);
    }
}
