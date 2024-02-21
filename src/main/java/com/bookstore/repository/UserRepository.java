package com.bookstore.repository;

import com.bookstore.entity.Address;
import com.bookstore.entity.Address_;
import com.bookstore.entity.User;
import com.bookstore.entity.User_;
import jakarta.persistence.criteria.Join;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Page<User> findAll(Specification<User> userSpec, Pageable pageable);

    @UtilityClass
    class Specs {

        public static Specification<User> withMinAge(Integer minAge) {
            return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(User_.AGE), minAge);
        }

        public static Specification<User> withMaxAge(Integer maxAge) {
            return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(User_.AGE), maxAge);
        }

        public static Specification<User> byCountry(String country) {
            return (root, query, cb) -> {
                Join<User, Address> join = root.join(User_.address);
                return cb.equal(join.get(Address_.COUNTRY), country);
            };
        }

        public static Specification<User> byCity(String city) {
            return (root, query, cb) -> {
                Join<User, Address> join = root.join(User_.address);
                return cb.equal(join.get(Address_.CITY), city);
            };
        }

        public static Specification<User> byRegion(String region) {
            return (root, query, cb) -> {
                Join<User, Address> join = root.join(User_.address);
                return cb.equal(join.get(Address_.REGION), region);
            };
        }
    }
}
