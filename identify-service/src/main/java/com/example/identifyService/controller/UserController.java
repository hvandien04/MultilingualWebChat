package com.example.identifyService.controller;

import com.example.identifyService.dto.request.UserCreateRequest;
import com.example.identifyService.dto.request.UserIdsRequest;
import com.example.identifyService.dto.request.UserUpdatePasswordRequest;
import com.example.identifyService.dto.request.UserUpdateRequest;
import com.example.identifyService.dto.response.ApiResponse;
import com.example.identifyService.dto.response.UserProfileResponse;
import com.example.identifyService.dto.response.UserResponse;
import com.example.identifyService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody UserCreateRequest userCreateRequest) throws Exception {
        return ApiResponse.<UserResponse>builder()
                .result(userService.CreateUser(userCreateRequest))
                .build();
    }

    @GetMapping
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.CheckMyInfo())
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserProfileResponse> getUserById(@PathVariable String userId) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userService.GetInfoByUserId(userId))
                .build();
    }

    @PutMapping
    ApiResponse<UserResponse> updateUser(@RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.UpdateUser(request))
                .build();
    }

    @PutMapping("/avatar")
    ApiResponse<String> updateAvatar(@RequestBody String url) {
        return ApiResponse.<String>builder()
                .message(userService.UpdateAvatar(url))
                .build();
    }

    @PostMapping("/batch")
    ApiResponse<List<UserProfileResponse>>getUserProfile(@RequestBody UserIdsRequest request){
        return ApiResponse.<List<UserProfileResponse>>builder()
                .result(userService.GetUserInfoByUserId(request))
                .build();
    }

    @PutMapping("/update-password")
    ApiResponse<String> updatePassword(@RequestBody UserUpdatePasswordRequest request){
        return ApiResponse.<String>builder()
                .message(userService.UpdatePassword(request))
                .build();
    }

    @PostMapping("/find-user")
    ApiResponse<List<UserProfileResponse>> findUser(@RequestParam String request){
        return ApiResponse.<List<UserProfileResponse>>builder()
                .result(userService.GetUserInfo(request))
                .build();
    }

}
