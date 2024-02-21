package com.bookstore.service.impl;

import com.bookstore.dto.publisher.PublisherDto;
import com.bookstore.dto.publisher.PublisherRequestDto;
import com.bookstore.dto.publisher.PublisherUpdateRequestDto;
import com.bookstore.entity.Publisher;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapping.PublisherMapper;
import com.bookstore.repository.PublisherRepository;
import com.bookstore.service.PublisherService;
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
public class PublisherServiceImpl implements PublisherService {
    private static final PublisherMapper MAPPER = PublisherMapper.INSTANCE;
    private final PublisherRepository publisherRepo;

    @Transactional(readOnly = true)
    @Override
    public Page<PublisherDto> findAll(Pageable pageable) {
        return publisherRepo.findAll(pageable)
                .map(MAPPER::mapToDto);
    }

    @Transactional(readOnly = true)
    @Override
    public PublisherDto findById(Long id) {
        var publisher = findPublisherEntity(id);
        return MAPPER.mapToDto(publisher);
    }

    @Override
    public PublisherDto create(PublisherRequestDto publisherRequestDto) {
        validatePublisherExist(publisherRequestDto.getName());
        var publisherEntity = MAPPER.mapToEntity(publisherRequestDto);
        var savedPublisher = publisherRepo.save(publisherEntity);
        return MAPPER.mapToDto(savedPublisher);
    }

    @Override
    public PublisherDto update(Long publisherId, PublisherUpdateRequestDto updateRequest) {
        var publisherEntity = findPublisherEntity(publisherId);
        var newName = updateRequest.getName();
        if (!publisherEntity.getName().equals(newName)) {
            validatePublisherExist(newName);
        }
        publisherEntity.setName(updateRequest.getName());
        return MAPPER.mapToDto(publisherRepo.save(publisherEntity));
    }

    @Override
    public void delete(Long publisherId) {
        var publisherEntity = findPublisherEntity(publisherId);
        if (!publisherEntity.getBooks().isEmpty()) {
            throw new ConflictException(
                    String.format("Can't delete publisher: [%d]. It has published books!", publisherId)
            );
        }

        publisherRepo.delete(publisherEntity);
    }

    private Publisher findPublisherEntity(Long publisherId) {
        return publisherRepo.findById(publisherId).orElseThrow(
                () -> {
                    log.warn("Publisher not found by id: [{}]", publisherId);
                    return new ResourceNotFoundException("Publisher not found by id: " + publisherId);
                }
        );
    }

    private void validatePublisherExist(String publisherName) {
        if (publisherRepo.existsByName(publisherName)) {
            throw new ConflictException(String.format("Publisher with name [%s] alredy exists.", publisherName));
        }
    }
}
