package com.bookstore.service;

import com.bookstore.dto.publisher.PublisherDto;
import com.bookstore.dto.publisher.PublisherRequestDto;
import com.bookstore.dto.publisher.PublisherUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PublisherService {
    Page<PublisherDto> findAll(Pageable pageable);

    PublisherDto findById(Long id);

    PublisherDto create(PublisherRequestDto publisherRequestDto);

    PublisherDto update(Long publisherId, PublisherUpdateRequestDto publisherUpdateRequestDto);

    void delete(Long publisherId);
}
