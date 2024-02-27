package com.bookstore.service;

import com.bookstore.controller.filters.UserFilter;
import com.bookstore.dto.user.UserDto;
import com.bookstore.dto.user.UserRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserDto> findAll(UserFilter userFilter, Pageable pageable);

    UserDto findById(Long id);

    UserDto create(UserRequestDto userRequestDto);

    UserDto update(Long userId, UserRequestDto userRequestDto);

    void delete(Long publisherId);
}
