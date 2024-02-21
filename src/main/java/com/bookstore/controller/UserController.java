package com.bookstore.controller;

import com.bookstore.controller.filters.UserFilter;
import com.bookstore.dto.user.UpdateUserRequestDto;
import com.bookstore.dto.user.UserDto;
import com.bookstore.dto.user.UserRequestDto;
import com.bookstore.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public Page<UserDto> findAllUsers(UserFilter userFilter, Pageable pageable) {
        return userService.findAll(userFilter, pageable);
    }

    @PostMapping
    public UserDto createUser(@Valid @RequestBody UserRequestDto userRequest) {
        return userService.create(userRequest);
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable(name = "id") Long id,
                              @Valid @RequestBody UpdateUserRequestDto updateRequest) {
        if (!id.equals(updateRequest.getId())) {
            throw new RuntimeException("Id in path should be equal to id in request body!");
        }

        return userService.update(id, updateRequest);
    }

    @GetMapping("/{id}")
    public UserDto findUser(@PathVariable(name = "id") Long id) {
        return userService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable(name = "id") Long id) {
        userService.delete(id);
    }
}
