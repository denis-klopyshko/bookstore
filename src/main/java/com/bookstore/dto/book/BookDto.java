package com.bookstore.dto.book;

import com.bookstore.dto.author.AuthorDto;
import com.bookstore.dto.publisher.PublisherDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookDto {
    private String isbn;
    private String title;
    private AuthorDto author;
    private PublisherDto publisher;
    private Integer year;
    private Double rating;
}
