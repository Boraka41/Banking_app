package com.example.banking.mapper;

import com.example.banking.dto.responses.UserResponse;
import com.example.banking.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponseDto(User user);

}
