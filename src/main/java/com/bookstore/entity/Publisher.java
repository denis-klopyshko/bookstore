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
@Builder
@Getter
@Setter
@Table(name = "publishers")
public class Publisher {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "publisherIdSequence")
    @SequenceGenerator(name = "publisherIdSequence", sequenceName = "publishers_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    @Builder.Default
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "publisher")
    private Set<Book> books = new HashSet<>();

    public void addBook(Book book) {
        this.books.add(book);
        book.setPublisher(this);
    }
}
