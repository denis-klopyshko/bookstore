package com.bookstore.mapping;

import com.bookstore.dto.publisher.PublisherDto;
import com.bookstore.dto.publisher.PublisherRequestDto;
import com.bookstore.entity.Publisher;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PublisherMapper {
    PublisherMapper INSTANCE = Mappers.getMapper(PublisherMapper.class);

    PublisherDto mapToDto(Publisher publisher);

    Publisher mapToEntity(PublisherRequestDto publisherRequest);
}
