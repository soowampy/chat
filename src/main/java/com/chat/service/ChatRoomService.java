package com.chat.service;

import static com.chat.controller.ChatRoomController.CHATROOM_ACTIVE_USERS;

import com.chat.dto.ChatRoomDTO;
import com.chat.model.ChatRoom;
import com.chat.repository.ChatRoomRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ChatRoomRepository chatRoomRepository;

    public List<ChatRoomDTO> getAllChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        List<ChatRoomDTO> chatRoomDTOs = new ArrayList<>();

        for (ChatRoom chatRoom : chatRooms) {
            int activeUsers = getActiveUsers(chatRoom.getId().toString());
            chatRoomDTOs.add(new ChatRoomDTO(chatRoom.getId(), chatRoom.getTitle(), activeUsers));
        }

        chatRoomDTOs.sort((a, b) -> {
            int activeUsersComparison = Integer.compare(b.getActiveUsers(), a.getActiveUsers());
            if (activeUsersComparison != 0) {
                return activeUsersComparison;
            }
            return a.getTitle().compareTo(b.getTitle());
        });

        return chatRoomDTOs;
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

    @Transactional
    public ChatRoom createChatRoom(String title) {
        ChatRoom chatRoom = new ChatRoom(title);
        chatRoomRepository.save(chatRoom);
        messagingTemplate.convertAndSend("/topic/chatrooms", chatRoom);
        return chatRoom;
    }
}
