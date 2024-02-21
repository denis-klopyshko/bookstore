package com.bookstore.controller.filters;

import com.bookstore.entity.Book;
import com.bookstore.repository.BookRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import static com.bookstore.SpecificationUtil.applyIfPresent;
import static org.springframework.data.jpa.domain.Specification.where;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookFilter {
    private String title;
    private Long authorId;
    private Long publisherId;
    private Integer year;

    public Specification<Book> toSpec() {
        return where(applyIfPresent(BookRepository.Specs::byAuthorId, authorId))
                .and(applyIfPresent(BookRepository.Specs::byPublisherId, publisherId))
                .and(applyIfPresent(BookRepository.Specs::byTitleLike, title))
                .and(applyIfPresent(BookRepository.Specs::byYear, year));
    }
}
