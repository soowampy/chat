package com.chat.service;

import static com.chat.controller.ChatRoomController.CHATROOM_ACTIVE_USERS;

import com.chat.controller.WebSocketController.JoinRoomPayload;
import com.chat.model.Message;
import com.chat.repository.MessageRepository;
import java.time.Instant;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public Message sendMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        messageRepository.save(message);
        log.info("message: {}", message.getContent());
        return message;
    }

    @Transactional
    public void joinRoom(JoinRoomPayload payload, String roomId) {
        String systemMessageContent = "--- " + payload.getNickname() + "님이 들어오셨습니다 ---";
        Message systemMessage = new Message(roomId, "system", null, systemMessageContent, true);
        messageRepository.save(systemMessage);
        messagingTemplate.convertAndSend("/topic/" + roomId, systemMessage);

        // 유저 수 증가
        redisTemplate.opsForZSet().add(CHATROOM_ACTIVE_USERS + ":" + roomId, payload.getUserId(), Instant.now().getEpochSecond());
        messagingTemplate.convertAndSend("/topic/activeUsers/" + roomId, getActiveUsers(roomId));
    }

    @Transactional
    public void leaveRoom(Message message, String roomId) {
        message.setSystemMessage(true);
        messageRepository.save(message);
        messagingTemplate.convertAndSend("/topic/" + roomId, message);

    }

    private int getActiveUsers(String roomId) {
        long now = Instant.now().getEpochSecond();
        long thirtyMinutesAgo = now - 1800; // 30분

        // 30분 이상 된 데이터들 제거
        redisTemplate.opsForZSet().removeRangeByScore(CHATROOM_ACTIVE_USERS + ":" + roomId, 0, thirtyMinutesAgo);

        // 남아있는 데이터 count 가져오기
        Set<String> activeUsers = redisTemplate.opsForZSet().rangeByScore(CHATROOM_ACTIVE_USERS + ":" + roomId, thirtyMinutesAgo, now);
        return activeUsers != null ? activeUsers.size() : 0;
    }
}
