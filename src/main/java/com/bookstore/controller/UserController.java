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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserRequestDto userRequest) {
        UserDto savedUser = userService.create(userRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.getId()).toUri();

        return ResponseEntity.created(location).body(savedUser);
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable(name = "id") Long id,
                              @Valid @RequestBody UpdateUserRequestDto updateRequest) {
        if (!id.equals(updateRequest.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id in path should be equal to id in request body!");
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
