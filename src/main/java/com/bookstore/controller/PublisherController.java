package com.bookstore.controller;

import com.bookstore.dto.publisher.PublisherDto;
import com.bookstore.dto.publisher.PublisherRequestDto;
import com.bookstore.dto.publisher.PublisherUpdateRequestDto;
import com.bookstore.service.PublisherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping("/v1/publishers")
public class PublisherController {

    private final PublisherService publisherService;

    @GetMapping
    public Page<PublisherDto> findAllPublishers(Pageable pageable) {
        return publisherService.findAll(pageable);
    }

    @PostMapping
    public PublisherDto createPublisher(@Valid @RequestBody PublisherRequestDto publisherRequest) {
        return publisherService.create(publisherRequest);
    }

    @PutMapping("/{id}")
    public PublisherDto updatePublisher(@PathVariable(name = "id") Long id,
                                        @Valid @RequestBody PublisherUpdateRequestDto updateRequest) {
        if (!id.equals(updateRequest.getId())) {
            throw new RuntimeException("Id in path should be equal to id in request body!");
        }

        return publisherService.update(id, updateRequest);
    }

    @GetMapping("/{id}")
    public PublisherDto findPublisher(@PathVariable(name = "id") Long id) {
        return publisherService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void deletePublisher(@PathVariable(name = "id") Long id) {
        publisherService.delete(id);
    }
}
