package com.bookstore.dto.book;

import com.bookstore.dto.author.AuthorDto;
import com.bookstore.dto.publisher.PublisherDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookRequestDto {

    @NotBlank
    private String isbn;

    @NotBlank
    private String title;

    @NotNull
    private AuthorDto author;

    @NotNull
    private PublisherDto publisher;

    @NotNull
    private Integer year;
}
