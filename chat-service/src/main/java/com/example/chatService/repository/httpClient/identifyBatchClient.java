package com.example.chatService.repository.httpClient;

import com.example.chatService.config.AuthenticationRequestInterceptor;
import com.example.chatService.dto.request.UserIdsRequest;
import com.example.chatService.dto.response.ApiResponse;
import com.example.chatService.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "identify-batch-client",url = "${app.identify.url}",configuration = {AuthenticationRequestInterceptor.class})
public interface identifyBatchClient {
    @PostMapping("/identify/users/batch")
    ApiResponse<List<UserProfileResponse>> getBatchInfo(@RequestBody UserIdsRequest userIdsRequest);
}
