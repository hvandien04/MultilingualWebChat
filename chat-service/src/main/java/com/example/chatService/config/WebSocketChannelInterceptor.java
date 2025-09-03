package com.example.chatService.config;

import com.example.chatService.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final CustomJwtDecoder customJwtDecoder;
    private final ConversationService conversationService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);

                try {
                    var jwt = customJwtDecoder.decode(token);
                    String userId = jwt.getClaimAsString("userId");

                    var auth = new UsernamePasswordAuthenticationToken(
                            userId,
                            token,
                            List.of()
                    );

                    accessor.setUser(auth);
                } catch (Exception e) {
                    throw new RuntimeException("JWT không hợp lệ trong WebSocket: " + e.getMessage());
                }
            }
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination(); // /topic/{conversationId}
            String conversationId = extractConversationId(destination);
            String userId = (String) accessor.getUser().getName();

            if (!conversationService.userHasAccessToConversation(userId, conversationId)) {
                throw new RuntimeException("Bạn không có quyền truy cập conversation này");
            }
        }

        return message;
    }

    private String extractConversationId(String destination) {
        if (destination != null && destination.startsWith("/topic/")) {
            return destination.substring("/topic/".length());
        }
        return null;
    }
}