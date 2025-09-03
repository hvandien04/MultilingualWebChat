package com.example.chatService.controller;

import com.example.chatService.dto.request.ConversationRequest;
import com.example.chatService.dto.request.CreateGroupRequest;
import com.example.chatService.dto.request.UserIdsRequest;
import com.example.chatService.dto.response.ApiResponse;
import com.example.chatService.dto.response.ConversationResponse;
import com.example.chatService.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping
@RestController
public class ConversationController {
    private final ConversationService conversationService;

    @PostMapping("/conversation")
    ApiResponse<ConversationResponse> createConversation(@RequestBody ConversationRequest conversationRequest) {
        return ApiResponse.<ConversationResponse>builder()
                .result(conversationService.createConversation(conversationRequest))
                .build();
    }

    @PostMapping("/conversation/group")
    ApiResponse<ConversationResponse> createGroupConversation(@RequestBody CreateGroupRequest request) {
        return ApiResponse.<ConversationResponse>builder()
                .result(conversationService.createConversationGroup(request))
                .build();
    }

    @PostMapping("/conversation/out")
    ApiResponse<String> outConversation(@RequestBody String conversationId) {
        return ApiResponse.<String>builder()
                .message(conversationService.outConversation(conversationId))
                .build();
    }

    @PostMapping("/{conversationId}/group/member")
    ApiResponse<String> addMemberToGroupConversation(@PathVariable String conversationId , @RequestBody UserIdsRequest request) {
        return ApiResponse.<String>builder()
                .message(conversationService.addMemberToGroupConversation(conversationId, request))
                .build();

    }

    @PutMapping("/{conversationId}/locale")
    ApiResponse<String> updateConversationLocale(@PathVariable String conversationId, @RequestBody String locale) {
        return ApiResponse.<String>builder()
                .message(conversationService.updateConversationLocale(conversationId, locale))
                .build();
    }

    @PutMapping("/{conversationId}/name")
    ApiResponse<String> updateConversationName(@PathVariable String conversationId, @RequestBody String name) {
        return ApiResponse.<String>builder()
                .message(conversationService.updateConversationName(conversationId, name))
                .build();
    }
}
