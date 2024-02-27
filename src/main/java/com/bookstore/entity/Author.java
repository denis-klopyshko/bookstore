package com.bookstore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "name"})
@ToString(of = {"id", "name"})
@Getter
@Setter
@Builder
@Table(name = "authors")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "authorIdSequence")
    @SequenceGenerator(name = "authorIdSequence", sequenceName = "authors_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Builder.Default
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private Set<Book> books = new HashSet<>();

    public void addBook(Book book) {
        this.books.add(book);
        book.setAuthor(this);
    }

    public static Author ofName(String name) {
        return Author.builder().name(name).build();
    }

    public static Author ofNameAndId(Long id, String name) {
        return Author.builder().id(id).name(name).build();
    }
}
