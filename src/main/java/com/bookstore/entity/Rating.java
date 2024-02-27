package com.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "score"})
@Getter
@Builder
@Setter
@Table(name = "ratings")
public class Rating {
    @EmbeddedId
    private BookRatingPrimaryKey id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("bookIsbn")
    @JoinColumn(name = "book_isbn", referencedColumnName = "isbn")
    private Book book;

    private Integer score;

    @Data
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class BookRatingPrimaryKey implements Serializable {
        @Column(name = "user_id")
        private Long userId;

        @Column(name = "book_isbn")
        private String bookIsbn;

        @Override
        public int hashCode() {
            return Objects.hash(userId, bookIsbn);
        }
    }
}
