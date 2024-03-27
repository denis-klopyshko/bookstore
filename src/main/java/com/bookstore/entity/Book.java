package com.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.NaturalId;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "isbn")
@Getter
@Setter
@Builder
@ToString
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bookIdSequence")
    @Column(name = "id", insertable = false, updatable = false)
    @SequenceGenerator(name = "bookIdSequence", sequenceName = "books_id_seq", allocationSize = 1)
    private Long id;

    @NaturalId
    @Column(name = "isbn", unique = true, nullable = false)
    private String isbn;

    @Column(name = "title")
    private String title;

    @ManyToOne
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;

    @Column(name = "year")
    private Integer year;

    @Builder.Default
    @ToString.Exclude
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "book",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<Rating> ratings = new HashSet<>();

    public void setAuthor(Author author) {
        this.author = author;
        author.getBooks().add(this);
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
        publisher.getBooks().add(this);
    }

    public void addRating(User user, int rating) {
        Rating newRating = new Rating(user, this, rating);
        ratings.add(newRating);
        user.getRatings().add(newRating);
    }

    public void removeRating(User user) {
        for (Iterator<Rating> iterator = ratings.iterator();
             iterator.hasNext(); ) {
            Rating rating = iterator.next();

            if (rating.getBook().equals(this) &&
                    rating.getUser().equals(user)) {
                iterator.remove();
                rating.getUser().getRatings().remove(rating);
                rating.setBook(null);
                rating.setUser(null);
            }
        }
    }

    public Book(String isbn, String title, Publisher publisher, Integer year) {
        this.isbn = isbn;
        this.title = title;
        this.publisher = publisher;
        this.year = year;
    }
}
