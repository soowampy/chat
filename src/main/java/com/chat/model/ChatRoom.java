package com.chat.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ChatRoom {
    @Id
    private String id;
    private String title;
    private LocalDateTime createdAt = LocalDateTime.now();
    @OneToMany(mappedBy = "chatRoom")
    private List<Message> messages;

    public ChatRoom(String title) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
    }
}
