package com.example.identifyService.service;

import com.example.identifyService.dto.request.*;
import com.example.identifyService.dto.response.UserProfileResponse;
import com.example.identifyService.dto.response.UserResponse;
import com.example.identifyService.entity.InvalidatedToken;
import com.example.identifyService.entity.User;
import com.example.identifyService.exception.AppException;
import com.example.identifyService.exception.ErrorCode;
import com.example.identifyService.mapper.UserMapper;
import com.example.identifyService.repository.InvalidatedTokenRepository;
import com.example.identifyService.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final IdGeneratorService idGeneratorService;
    private final EmailService emailService;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final AuthenticationService authenticationService;

    @NonFinal
    @Value("${spring.activation.base-url}")
    private String baseActiveLink;

    public UserResponse CreateUser(UserCreateRequest userCreateRequest) throws Exception {
        User user = userMapper.toUser(userCreateRequest);
        if(userCreateRequest.getPassword() == null || userCreateRequest.getPassword().trim().isEmpty()){
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setUserId(idGeneratorService.generateRandomId("US", userRepository::existsById));
        user.setPassword(passwordEncoder.encode(userCreateRequest.getPassword()));
        user.setRole("USER");
        user.setLocale("US");
        String url = generateActiveLink(user);
        userRepository.save(user);
        emailService.sendActivationLink(user.getEmail(), user.getUsername(), url);
        return userMapper.toUserResponse(user);
    }

    private String generateActiveLink(User user) {
        String token = authenticationService.generateToken(user);
        System.out.println("Token: " + token);
        return baseActiveLink + "?token=" + token;
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
            return "Password is not correct. Please try again.";
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

    public List<UserProfileResponse> GetUserInfo(String request) {
        if (request == null || request.trim().isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if (request.matches(emailRegex)) {
            return userRepository.findByEmail(request)
                    .map(user -> List.of(userMapper.toUserProfileResponse(user)))
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        } else {
            return userRepository.findById(request)
                    .map(user -> List.of(userMapper.toUserProfileResponse(user)))
                    .orElseGet(() ->
                            userRepository.findAllByFullNameContainingIgnoreCase(request).stream()
                                    .map(userMapper::toUserProfileResponse)
                                    .toList()
                    );
        }
    }

    public String UpdateAvatar(String url) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setAvatarUrl(url);
        userRepository.save(user);
        return "Update avatar successfully";
    }

    public void activateUser(String token) throws ParseException, JOSEException {
        var signToken = authenticationService.verifyToken(token,false);
        String accessTokenUsername = signToken.getJWTClaimsSet().getSubject();
        String accessTokenId = signToken.getJWTClaimsSet().getJWTID();
        Date accessTokenExpiryTime = signToken.getJWTClaimsSet().getExpirationTime();
        User user = userRepository.findByUsername(accessTokenUsername).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));

        InvalidatedToken invalidatedToken= InvalidatedToken.builder()
                .id(accessTokenId)
                .expiryTime(accessTokenExpiryTime)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);
        if(user.getIsActive() == true){
            throw new AppException(ErrorCode.ALREADY_ACTIVATE);
        }
        if(accessTokenExpiryTime.before(new Date())){
            throw new AppException(ErrorCode.EXPIRED_TOKEN);
        }
        user.setIsActive(true);
        userRepository.save(user);
    }
}
