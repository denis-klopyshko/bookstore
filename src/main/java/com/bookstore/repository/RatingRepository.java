package com.bookstore.repository;

import com.bookstore.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Page<Rating> findAllByBookIsbn(String isbn, Pageable pageable);

    Page<Rating> findAllByUserId(Long userId, Pageable pageable);
}
