package com.chat.controller;

import com.chat.model.Message;
import com.chat.repository.MessageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public final class MessageController {

    private final MessageRepository messageRepository;

    /**
     * 해당 채팅방에 있는 모든 메시지를 반환한다
     * */
    @GetMapping("/chatroom/{chatRoomId}")
    public List<Message> getMessagesByChatRoomId(@PathVariable String chatRoomId) {
        return messageRepository.findByChatRoomId(chatRoomId);
    }
}
