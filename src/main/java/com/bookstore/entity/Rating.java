package com.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"user", "book"})
@ToString(of = {"id", "score"})
@Getter
@Builder
@Setter
@Table(name = "ratings")
public class Rating {
    @EmbeddedId
    private BookRatingPrimaryKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookIsbn")
    @JoinColumn(name = "book_isbn", referencedColumnName = "isbn")
    private Book book;

    private Integer score;

    public Rating(User user, Book book, Integer score) {
        this.user = user;
        this.book = book;
        this.score = score;
    }

    @Data
    @EqualsAndHashCode(of = {"userId", "bookIsbn"})
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class BookRatingPrimaryKey implements Serializable {
        @Column(name = "user_id")
        private Long userId;

        @Column(name = "book_isbn")
        private String bookIsbn;
    }
}
