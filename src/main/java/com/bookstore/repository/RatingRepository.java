package com.bookstore.repository;

import com.bookstore.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Page<Rating> findAllByBookIsbn(String isbn, Pageable pageable);

    List<Rating> findAllByBookIsbnIn(List<String> isbns);

    @Query(value = "SELECT r.id.bookIsbn, ROUND(AVG(r.score), 2) FROM Rating r WHERE r.id.bookIsbn IN :bookIsbns GROUP BY r.id.bookIsbn")
    List<Object[]> findAverageRatingByBookIsbnsIn(List<String> bookIsbns);

    Page<Rating> findAllByUserId(Long userId, Pageable pageable);
}
