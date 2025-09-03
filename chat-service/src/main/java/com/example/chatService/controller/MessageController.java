package com.example.chatService.controller;

import com.example.chatService.dto.response.ApiResponse;
import com.example.chatService.dto.response.MessageResponse;
import com.example.chatService.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/message")
public class MessageController {
    private final MessageService messageService;

    @GetMapping("/{conversationId}")
    ApiResponse<List<MessageResponse>> getHistoryMessage(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size, @PathVariable String conversationId) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        return ApiResponse.<List<MessageResponse>>builder()
                .result(messageService.getHistoryMessage(conversationId, pageable))
                .build();

    }

    @GetMapping("/list")
    ApiResponse<List<MessageResponse>> getHistoryMessage(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        return ApiResponse.<List<MessageResponse>>builder()
                .result(messageService.getMyMessage(pageable))
                .build();
    }
}
