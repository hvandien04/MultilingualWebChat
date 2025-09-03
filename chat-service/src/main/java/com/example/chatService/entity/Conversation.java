package com.example.chatService.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "conversation")
public class Conversation {
    @Id
    @Size(max = 50)
    @Column(name = "conversation_id", nullable = false, length = 50)
    private String conversationId;

    @Size(max = 100)
    @Column(name = "conversation_name", length = 100)
    private String conversationName;

    @Size(max = 20)
    @Column(name = "locale", length = 20)
    private String locale;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = Instant.now();
    }

}