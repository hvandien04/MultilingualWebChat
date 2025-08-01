package com.example.gateway.service;

import com.example.gateway.dto.request.IntrospectRequest;
import com.example.gateway.dto.response.ApiResponse;
import com.example.gateway.dto.response.IntrospectResponse;
import com.example.gateway.repository.IdentifyClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class IdentifyService {
    private final IdentifyClient identifyClient;
    public Mono<ApiResponse<IntrospectResponse>> introspect(String token) {
        return identifyClient.introspect(IntrospectRequest.builder()
                        .token(token)
                .build());
    }
}
