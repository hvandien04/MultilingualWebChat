package com.example.chatService.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "group_member")
public class GroupMember {
    @EmbeddedId
    private GroupMemberId id;

    @MapsId("conversationId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "joined_datetime", nullable = false)
    private Instant joinedDatetime;

    @Column(name = "left_datetime")
    private Instant leftDatetime;

    @PrePersist
    void prePersist() {
        this.joinedDatetime = Instant.now();
    }

}