package com.bookstore.mapping;

import com.bookstore.dto.user.UserDto;
import com.bookstore.dto.user.UserRequestDto;
import com.bookstore.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "city", source = "user.address.city")
    @Mapping(target = "country", source = "user.address.country")
    @Mapping(target = "region", source = "user.address.region")
    @Mapping(target = "id", source = "id")
    UserDto mapToDto(User user);

    User mapToEntity(UserRequestDto userRequestDto);
}
