package com.bookstore.mapping;

import com.bookstore.dto.book.BookDto;
import com.bookstore.dto.book.BookRequestDto;
import com.bookstore.dto.book.BookShortDto;
import com.bookstore.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BookMapper {
    BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);

    @Mapping(target = "rating", ignore = true)
    BookDto mapToDto(Book book);

    BookShortDto mapToShortDto(Book book);

    @Mapping(target = "author", ignore = true)
    @Mapping(target = "publisher", ignore = true)
    Book mapToEntity(BookRequestDto bookRequestDto);
}
