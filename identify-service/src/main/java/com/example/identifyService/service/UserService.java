package com.example.identifyService.service;

import com.example.identifyService.dto.request.*;
import com.example.identifyService.dto.response.UserProfileResponse;
import com.example.identifyService.dto.response.UserResponse;
import com.example.identifyService.entity.User;
import com.example.identifyService.exception.AppException;
import com.example.identifyService.exception.ErrorCode;
import com.example.identifyService.mapper.UserMapper;
import com.example.identifyService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final IdGeneratorService idGeneratorService;

    public UserResponse CreateUser(UserCreateRequest userCreateRequest) {
        User user = userMapper.toUser(userCreateRequest);
        if(userCreateRequest.getPassword() == null || userCreateRequest.getPassword().trim().isEmpty()){
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setUserId(idGeneratorService.generateRandomId("US", userRepository::existsById));
        user.setPassword(passwordEncoder.encode(userCreateRequest.getPassword()));
        user.setRole("USER");
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PostAuthorize("returnObject.username==authentication.name")
    public UserResponse CheckMyInfo(){
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    public String UpdatePassword(UserUpdatePasswordRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "Change password successfully";
    }

    public UserResponse UpdateUser(UserUpdateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUser(user, request);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserProfileResponse GetInfoByUserId(String userId) {
        return userRepository.findById(userId).map(userMapper::toUserProfileResponse)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    public List<UserProfileResponse> GetUserInfoByUserId(UserIdsRequest request) {
        return userRepository.findAllById(request.getUserIds()).stream()
                .map(userMapper::toUserProfileResponse)
                .toList();
    }
}
