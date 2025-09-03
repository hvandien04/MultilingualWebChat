package com.example.chatService.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse {
    private String messageId;
    private String conversationName;
    private String groupLocale;
    private String userId;
    private String conversationId;
    private String messageText;
    private String messageTextTranslate;
    private Instant sentDatetime;
    private String type;
    private List<UserProfileResponse> userProfiles;
}
