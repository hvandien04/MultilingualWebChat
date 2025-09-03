package com.example.chatService.service;

import com.example.chatService.dto.request.UserIdsRequest;
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
import com.example.chatService.repository.httpClient.identifyBatchClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MessageMapper messageMapper;
    private final identifyBatchClient identifyBatchClient;
    private final ConversationRepository conversationRepository;

    public List<MessageResponse> getHistoryMessage(String conversationId, Pageable pageable) {
        Conversation conversation = conversationRepository.findByConversationId(conversationId);
        return messageRepository.findAllByConversationOrderBySentDatetimeDesc(conversation, pageable).stream().map(messageMapper::toMessageResponse).toList();
    }


    public List<MessageResponse> getMyMessage(Pageable pageable) {
        String userId = getUserId();

        List<Message> messages = messageRepository.findLastMessagesByUserId(userId, pageable);

        Map<String, List<String>> conversationToOtherUsers = messages.stream()
                .collect(Collectors.toMap(
                        m -> m.getConversation().getConversationId(),
                        m -> groupMemberRepository.getGroupMembersByConversationIdExcludeUser(
                                m.getConversation().getConversationId(), userId)
                ));

        Set<String> allOtherUserIds = conversationToOtherUsers.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        UserIdsRequest userIdsRequest = UserIdsRequest.builder()
                .userIds(new ArrayList<>(allOtherUserIds))
                .build();

        ApiResponse<List<UserProfileResponse>> userResponses = identifyBatchClient.getBatchInfo(userIdsRequest);

        if (userResponses.getResult().isEmpty()) {
            throw new RuntimeException("Không lấy được thông tin người dùng");
        }

        Map<String, UserProfileResponse> userMap = userResponses.getResult().stream()
                .collect(Collectors.toMap(UserProfileResponse::getUserId, u -> u));

        return messages.stream()
                .map(m -> {
                    String conversationId = m.getConversation().getConversationId();
                    List<UserProfileResponse> otherProfiles = conversationToOtherUsers
                            .getOrDefault(conversationId, List.of()).stream()
                            .map(userMap::get)
                            .filter(Objects::nonNull)
                            .toList();

                    return MessageResponse.builder()
                            .messageText(m.getMessageText())
                            .messageTextTranslate(m.getMessageTextTranslate())
                            .sentDatetime(m.getSentDatetime())
                            .userId(m.getUserId())
                            .conversationId(conversationId)
                            .userProfiles(otherProfiles)
                            .conversationName(m.getConversation().getConversationName())
                            .groupLocale(m.getConversation().getLocale())
                            .build();
                })
                .toList();
    }

    private String getUserId() {
        String userId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication.getPrincipal() instanceof Jwt jwt) {
            userId = jwt.getClaimAsString("userId");
        }
        if(userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return userId;
    }
}
