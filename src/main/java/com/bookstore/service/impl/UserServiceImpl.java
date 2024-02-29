package com.bookstore.service.impl;

import com.bookstore.controller.filters.UserFilter;
import com.bookstore.dto.user.UserDto;
import com.bookstore.dto.user.UserRequestDto;
import com.bookstore.entity.Address;
import com.bookstore.entity.User;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapping.UserMapper;
import com.bookstore.repository.UserRepository;
import com.bookstore.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class UserServiceImpl implements UserService {
    private static final UserMapper MAPPER = UserMapper.INSTANCE;
    private final UserRepository userRepo;

    @Override
    public Page<UserDto> findAll(UserFilter userFilter, Pageable pageable) {
        return userRepo
                .findAll(userFilter.toSpec(), pageable)
                .map(MAPPER::mapToDto);
    }

    @Override
    public UserDto findById(Long id) {
        var user = findUserEntity(id);
        return MAPPER.mapToDto(user);
    }

    @Override
    public UserDto create(UserRequestDto userRequestDto) {
        var userEntity = MAPPER.mapToEntity(userRequestDto);
        var userAddress = Address.builder().region(userRequestDto.getRegion())
                .country(userRequestDto.getCountry())
                .city(userRequestDto.getCity())
                .build();
        userEntity.setAddress(userAddress);
        var savedUser = userRepo.save(userEntity);
        return MAPPER.mapToDto(savedUser);
    }

    @Override
    public UserDto update(Long userId, UserRequestDto userRequestDto) {
        var currentUser = findUserEntity(userId);

        if (!Objects.equals(currentUser.getAge(), userRequestDto.getAge())) {
            currentUser.setAge(userRequestDto.getAge());
        }

        Optional.ofNullable(userRequestDto.getCity()).ifPresent(city -> {
            currentUser.getAddress().setCity(city);
        });

        Optional.ofNullable(userRequestDto.getCountry()).ifPresent(country -> {
            currentUser.getAddress().setCountry(country);
        });

        Optional.ofNullable(userRequestDto.getRegion()).ifPresent(region -> {
            currentUser.getAddress().setRegion(region);
        });

        return MAPPER.mapToDto(userRepo.save(currentUser));
    }

    @Override
    public void delete(Long userId) {
        var user = findUserEntity(userId);
        userRepo.delete(user);
    }

    private User findUserEntity(Long userId) {
        return userRepo.findById(userId).orElseThrow(
                () -> {
                    log.warn("User not found by id: [{}]", userId);
                    return new ResourceNotFoundException("User not found by id: " + userId);
                }
        );
    }
}
