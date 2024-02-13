package com.bookstore.repository;

import com.bookstore.entity.Author;
import com.bookstore.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long> {
    Optional<Publisher> findByName(String name);

    Set<Publisher> findByNameIn(Set<String> names);
}
