package com.example.chatService.service;

import com.example.chatService.dto.request.ConversationRequest;
import com.example.chatService.dto.request.CreateGroupRequest;
import com.example.chatService.dto.request.UserIdsRequest;
import com.example.chatService.dto.response.ConversationResponse;
import com.example.chatService.entity.Conversation;
import com.example.chatService.entity.GroupMember;
import com.example.chatService.entity.GroupMemberId;
import com.example.chatService.exception.AppException;
import com.example.chatService.exception.ErrorCode;
import com.example.chatService.repository.ConversationRepository;
import com.example.chatService.repository.GroupMemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor

public class ConversationService {

    private final IdGeneratorService idGeneratorService;
    private final GroupMemberRepository groupMemberRepository;
    private final ConversationRepository conversationRepository;

    public boolean userHasAccessToConversation(String userId, String conversationId) {
        Optional<String> conversation = groupMemberRepository.findConversationIdByUserIdAndConversationId(userId, conversationId);
        return conversation.isPresent();
    }

    @Transactional
    public ConversationResponse createConversation(ConversationRequest conversationRequest) {
        String userId = getUserId();
        Optional<String> conversationId = groupMemberRepository.findConversationIdBetweenTwoUsers(conversationRequest.getToUserId(), userId);
        if(conversationId.isPresent()) {
            return ConversationResponse.builder().ConversationId(conversationId.get()).build();
        } else {
            String newConversationId = idGeneratorService.generateRandomId("C_", conversationRepository::existsById);
            Conversation conversation = Conversation.builder()
                    .conversationId(newConversationId)
                    .build();
            log.info("Create conversation: " + conversation.getConversationId());
            conversationRepository.save(conversation);
            GroupMember groupMember1 = GroupMember.builder()
                    .conversation(conversation)
                    .id(GroupMemberId.builder()
                            .conversationId(conversation.getConversationId())
                            .userId(conversationRequest.getToUserId())
                            .build())
                    .build();
            GroupMember groupMember2 = GroupMember.builder()
                    .conversation(conversation)
                    .id(GroupMemberId.builder()
                            .conversationId(conversation.getConversationId())
                            .userId(userId)
                            .build())
                    .build();
            groupMemberRepository.saveAll(List.of(groupMember1, groupMember2));
            return ConversationResponse.builder()
                    .ConversationId(newConversationId)
                    .build();
        }
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

    @Transactional
    public ConversationResponse createConversationGroup(CreateGroupRequest request) {
        Conversation conversation = Conversation.builder()
                .conversationName(request.getConversationName())
                .locale(request.getLocale())
                .conversationId(idGeneratorService.generateRandomId("C_", conversationRepository::existsById))
                .build();
        conversationRepository.save(conversation);
        List<GroupMember> members = request.getUserIds().stream()
                .map(userId -> GroupMember.builder()
                        .id(new GroupMemberId(userId, conversation.getConversationId()))
                        .conversation(conversation)
                        .build())
                .toList();
        groupMemberRepository.saveAll(members);
        return ConversationResponse.builder()
                .ConversationId(conversation.getConversationId())
                .build();
    }

    public String updateConversationLocale(String conversationId, String locale) {
        Conversation conversation = conversationRepository.findByConversationId(conversationId);
        if(conversation == null) {
            throw new AppException(ErrorCode.CONVERSATION_NOT_FOUND);
        }
        conversation.setLocale(locale);
        conversationRepository.save(conversation);
        return "Locale update success";
    }

    public String updateConversationName(String conversationId, String name) {
        Conversation conversation = conversationRepository.findByConversationId(conversationId);
        if(conversation == null) {
            throw new AppException(ErrorCode.CONVERSATION_NOT_FOUND);
        }
        conversation.setConversationName(name);
        conversationRepository.save(conversation);
        return "Name updated success";
    }

    public String addMemberToGroupConversation(String conversationId, UserIdsRequest request) {
        Conversation conversation = conversationRepository.findByConversationId(conversationId);
        if(conversation == null) {
            throw new AppException(ErrorCode.CONVERSATION_NOT_FOUND);
        }
        List<GroupMember> members = request.getUserIds().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .filter(userId -> !groupMemberRepository
                        .existsByIdUserIdAndIdConversationId(userId, conversationId))
                .map(userId -> GroupMember.builder()
                        .id(new GroupMemberId(userId, conversationId))
                        .conversation(conversation)
                        .build())
                .toList();
        groupMemberRepository.saveAll(members);
        return "User added to the conversation.";
    }

    public String outConversation(String conversationId) {
        String UserId = getUserId();
        GroupMember groupMember = groupMemberRepository.findByIdUserIdAndIdConversationId(UserId, conversationId).orElseThrow(
                () -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        groupMemberRepository.delete(groupMember);
        return "Out conversation success";
    }
}
