package com.example.chatService.service;

import com.example.chatService.constant.MessageType;
import com.example.chatService.dto.request.MessageRequest;
import com.example.chatService.dto.request.TranslateRequest;
import com.example.chatService.dto.response.ApiResponse;
import com.example.chatService.dto.response.MessageResponse;
import com.example.chatService.dto.response.UserProfileResponse;
import com.example.chatService.entity.Conversation;
import com.example.chatService.entity.Message;
import com.example.chatService.exception.AppException;
import com.example.chatService.exception.ErrorCode;
import com.example.chatService.mapper.MessageMapper;
import com.example.chatService.repository.ConversationRepository;
import com.example.chatService.repository.GroupMemberRepository;
import com.example.chatService.repository.MessageRepository;
import com.example.chatService.repository.httpClient.identifyClient;
import com.example.chatService.repository.httpClient.translateClient;
import com.example.dto.TranslateResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final translateClient translateClient;
    private final identifyClient identifyClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(MessageRequest messageRequest, Principal principal) {
        String userId = principal.getName();

        Conversation conversation = conversationRepository.findById(messageRequest.getConversationId())
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        MessageType messageType = MessageType.valueOf(messageRequest.getType().toUpperCase());

        Message msg = Message.builder()
                .conversation(conversation)
                .userId(userId)
                .type(messageType.name())
                .messageText(messageRequest.getMessageText())
                .build();
        messageRepository.save(msg);
        MessageResponse response = messageMapper.toMessageResponse(msg);
        simpMessagingTemplate.convertAndSend("/topic/" + messageRequest.getConversationId(), response);
        if(messageType == MessageType.TEXT){
            kafkaTemplate.send("translate-group",msg.getId().toString());
        }

    }

    @Transactional
    public void translateCall(String messageId) {
        String targetLocale;
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found: " + messageId) );
        if(message.getConversation().getLocale() != null) {
            targetLocale = message.getConversation().getLocale();
        } else {
            String toUserId = groupMemberRepository.getGroupMembersByUserId(message.getUserId(), message.getConversation().getConversationId());
            if (toUserId == null) {
                throw new AppException(ErrorCode.CONVERSATION_NOT_FOUND);
            }
            ApiResponse<UserProfileResponse> user = identifyClient.getInfo(toUserId);
            targetLocale = user.getResult().getLocale();
        }
        System.out.println("Translate call: " + messageId + " " + targetLocale);
        String translated = translate(message.getMessageText(),targetLocale);
        message.setMessageTextTranslate(translated);
        messageRepository.save(message);
        simpMessagingTemplate.convertAndSend("/topic/" + message.getConversation().getConversationId(), messageMapper.toMessageResponse(message));
    }

    private String translate(String messageText, String locale) {
        TranslateRequest request = TranslateRequest.builder()
                .contents(List.of(
                        TranslateRequest.Content.builder()
                                .parts(List.of(
                                        TranslateRequest.Part.builder()
                                                .text("You are a translation tool. Translate the following sentence into the language of the country code"+ locale + " and return only the result: " + messageText)
                                                .build()
                                ))
                                .build()
                ))
                .build();
        TranslateResponse response = translateClient.translate(request);
        return response.getCandidates().getFirst().getContent().getParts().getFirst().getText();
    }
}
