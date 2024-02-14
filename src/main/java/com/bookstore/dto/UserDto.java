package com.bookstore.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long id;
    private String city;
    private String region;
    private String country;
    private Integer age;
}
