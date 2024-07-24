package com.chat.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Entity
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String chatRoomId;
    private String userId;
    private String nickname;
    private String content;
    private LocalDateTime createdAt = LocalDateTime.now();
    private boolean isSystemMessage;

    @ManyToOne
    @JoinColumn(name = "chatRoomId", insertable = false, updatable = false)
    @JsonBackReference
    private ChatRoom chatRoom;

    public Message(String chatRoomId, String userId, String nickname, String content, boolean isSystemMessage) {
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.nickname = nickname;
        this.content = content;
        this.isSystemMessage = isSystemMessage;
    }
}
