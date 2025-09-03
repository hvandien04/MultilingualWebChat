package com.example.chatService.repository.httpClient;

import com.example.chatService.config.WebSocketFeignInterceptor;
import com.example.chatService.dto.response.ApiResponse;
import com.example.chatService.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "IdentifyClient",url = "${app.identify.url}", configuration = WebSocketFeignInterceptor.class)
public interface identifyClient {
    @GetMapping("/identify/users/{userId}")
    ApiResponse<UserProfileResponse> getInfo(@PathVariable String userId);
}
