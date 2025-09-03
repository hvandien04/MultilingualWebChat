package com.example.chatService.controller;

import com.example.chatService.dto.request.MessageRequest;
import com.example.chatService.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat")
    public void chat(@Payload MessageRequest messageRequest, Principal principal) {
        chatService.sendMessage(messageRequest, principal);

    }

    @KafkaListener(topics = "translate-group", groupId = "translate-group")
    public void receiveMessage(String messageId) {
        chatService.translateCall(messageId);
    }
}
