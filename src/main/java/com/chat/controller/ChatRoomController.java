package com.chat.controller;

import com.chat.dto.ChatRoomDTO;
import com.chat.model.ChatRoom;
import com.chat.service.ChatRoomService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
@Slf4j
public final class ChatRoomController {

    private final ChatRoomService chatRoomService;
    public static final String CHATROOM_ACTIVE_USERS = "CHATROOM_ACTIVE_USERS";

    /**
     * 채팅방 목록을 가져온다.
     * */
    @GetMapping
    public ResponseEntity<List<ChatRoomDTO>> getAllChatRooms() {
        return ResponseEntity.ok(chatRoomService.getAllChatRooms());
    }
}
