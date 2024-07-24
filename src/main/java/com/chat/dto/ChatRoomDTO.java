package com.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRoomDTO {
    private String id;
    private String title;
    private int activeUsers;

    public ChatRoomDTO(String id, String title, int activeUsers) {
        this.id = id;
        this.title = title;
        this.activeUsers = activeUsers;
    }
}
