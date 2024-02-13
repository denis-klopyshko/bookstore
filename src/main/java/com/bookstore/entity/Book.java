package com.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "book_entity_seq")
    @Column(name = "id", insertable = false, updatable = false)
    @SequenceGenerator(name = "book_entity_seq", allocationSize = 1)
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
    @OneToMany(mappedBy = "book")
    private Set<Rating> ratings = new HashSet<>();

    public Book(String isbn, String title, Publisher publisher, Integer year) {
        this.isbn = isbn;
        this.title = title;
        this.publisher = publisher;
        this.year = year;
    }
}
