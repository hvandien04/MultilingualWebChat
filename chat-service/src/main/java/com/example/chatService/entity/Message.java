package com.example.chatService.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Size(max = 50)
    @NotNull
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @NotNull
    @Lob
    @Column(name = "message_text", nullable = false)
    private String messageText;

    @Lob
    @Column(name = "message_text_translate")
    private String messageTextTranslate;

    @NotNull
    @Column(name = "type", nullable = false)
    private String type;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "sent_datetime", nullable = false)
    private Instant sentDatetime;

    @PrePersist
    private void prePersist() {
        this.sentDatetime = Instant.now();
    }

}