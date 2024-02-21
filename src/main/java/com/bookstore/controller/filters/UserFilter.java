package com.bookstore.controller.filters;

import com.bookstore.entity.User;
import com.bookstore.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import static com.bookstore.SpecificationUtil.applyIfPresent;
import static org.springframework.data.jpa.domain.Specification.where;

@Data
@Builder
@AllArgsConstructor
@Slf4j
@NoArgsConstructor
public class UserFilter {
    private Integer minAge;
    private Integer maxAge;
    private String country;
    private String region;
    private String city;

    public Specification<User> toSpec() {
        return where(applyIfPresent(UserRepository.Specs::withMinAge, minAge))
                .and(applyIfPresent(UserRepository.Specs::withMaxAge, maxAge))
                .and(applyIfPresent(UserRepository.Specs::byCountry, country))
                .and(applyIfPresent(UserRepository.Specs::byRegion, region))
                .and(applyIfPresent(UserRepository.Specs::byCity, city));
    }
}
