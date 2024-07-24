package com.chat.controller;

import com.chat.model.Message;
import com.chat.service.WebSocketService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;


@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final WebSocketService webSocketService;

    /**
     * 메시지를 보내고, 저장한 후 전달한다.
     * */
    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ResponseEntity<Message> sendMessage(@Payload Message message) {
        return ResponseEntity.ok().body(webSocketService.sendMessage(message));
    }

    /**
     * 사용자가 채팅방에 참여했을 때 알림 메시지를 보낸다
     * */
    @MessageMapping("/chat.joinRoom/{roomId}")
    public ResponseEntity<?> joinRoom(@Payload JoinRoomPayload payload, @DestinationVariable String roomId) {
        webSocketService.joinRoom(payload, roomId);
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자가 채팅방을 떠날 때 알림 메시지를 보낸다
     * */
    @MessageMapping("/chat.leaveRoom/{roomId}")
    public ResponseEntity<?> leaveRoom(@Payload Message message, @DestinationVariable String roomId) {
        webSocketService.leaveRoom(message, roomId);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class JoinRoomPayload {
        private String userId;
        private String nickname;
    }
}
