package com.bookstore.mapping;

import com.bookstore.dto.author.AuthorDto;
import com.bookstore.dto.author.AuthorRequestDto;
import com.bookstore.entity.Author;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthorMapper {
    AuthorMapper INSTANCE = Mappers.getMapper(AuthorMapper.class);

    AuthorDto mapToDto(Author author);

    Author mapToEntity(AuthorRequestDto authorRequestDto);
}
