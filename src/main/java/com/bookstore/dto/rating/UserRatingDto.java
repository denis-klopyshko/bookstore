package com.bookstore.dto.rating;

import com.bookstore.dto.book.BookShortDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRatingDto {
    private BookShortDto book;
    private Integer score;
}
