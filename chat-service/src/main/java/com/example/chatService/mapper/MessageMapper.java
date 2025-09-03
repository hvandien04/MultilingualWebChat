package com.example.chatService.mapper;

import com.example.chatService.dto.response.MessageResponse;
import com.example.chatService.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(target = "conversationId", source = "conversation.conversationId")
    @Mapping(target = "messageId", source = "id")
    @Mapping(target = "conversationName", source = "conversation.conversationName")
    MessageResponse toMessageResponse(Message message);
}
