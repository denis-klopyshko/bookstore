package com.bookstore.controller;

import com.bookstore.controller.filters.UserFilter;
import com.bookstore.dto.user.UpdateUserRequestDto;
import com.bookstore.dto.user.UserDto;
import com.bookstore.dto.user.UserRequestDto;
import com.bookstore.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
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
    public Page<UserDto> findAllUsers(@ParameterObject UserFilter userFilter, @ParameterObject Pageable pageable) {
        return userService.findAll(userFilter, pageable);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserRequestDto userRequest) {
        UserDto savedUser = userService.create(userRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.getId()).toUri();

        return ResponseEntity.created(location).body(savedUser);
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
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

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable(name = "id") Long id) {
        userService.delete(id);
    }
}
