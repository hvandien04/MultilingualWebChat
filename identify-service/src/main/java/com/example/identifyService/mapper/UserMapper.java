package com.example.identifyService.mapper;

import com.example.identifyService.dto.request.UserCreateRequest;
import com.example.identifyService.dto.request.UserUpdateRequest;
import com.example.identifyService.dto.response.UserProfileResponse;
import com.example.identifyService.dto.response.UserResponse;
import com.example.identifyService.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreateRequest request);

    UserResponse toUserResponse(User user);

    UserProfileResponse toUserProfileResponse(User user);

    void updateUser(@MappingTarget User user, UserUpdateRequest request);

}
