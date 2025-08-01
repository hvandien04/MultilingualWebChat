package com.example.identifyService.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserCreateRequest {
    private String username;
    private String email;
    private String password;
    private String fullName;
}
