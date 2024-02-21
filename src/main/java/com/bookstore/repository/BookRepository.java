package com.bookstore.repository;

import com.bookstore.entity.Author_;
import com.bookstore.entity.Book;
import com.bookstore.entity.Book_;
import com.bookstore.entity.Publisher_;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.book = :book")
    Optional<Double> calculateAverageRating(@Param("book") Book book);

    Page<Book> findAll(Specification<Book> spec, Pageable pageable);

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String title);

    @UtilityClass
    class Specs {
        public static Specification<Book> byAuthorId(Long authorId) {
            return ((root, query, cb) ->
                    cb.equal(root.join(Book_.AUTHOR).get(Author_.ID), authorId));
        }

        public static Specification<Book> byPublisherId(Long publisherId) {
            return ((root, query, cb) ->
                    cb.equal(root.join(Book_.PUBLISHER).get(Publisher_.ID), publisherId));
        }

        public static Specification<Book> byTitleLike(String bookTitle) {
            return ((root, query, cb) ->
                    cb.like(root.get(Book_.TITLE), '%' + bookTitle + '%'));
        }

        public static Specification<Book> byYear(Integer year) {
            return ((root, query, cb) ->
                    cb.equal(root.get(Book_.YEAR), year));
        }
    }
}
