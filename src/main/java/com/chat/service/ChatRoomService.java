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
        Set<TypedTuple<String>> activeUserCounts = redisTemplate.opsForZSet()
            .reverseRangeWithScores(CHATROOM_ACTIVE_USERS, 0, -1);

        List<String> roomIdsSortedByActiveUsers = activeUserCounts.stream()
            .map(ZSetOperations.TypedTuple::getValue)
            .collect(Collectors.toList());

        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        List<ChatRoomDTO> chatRoomDTOs = new ArrayList<>();

        for (ChatRoom chatRoom : chatRooms) {
            int activeUsers = getActiveUsers(chatRoom.getId().toString());
            chatRoomDTOs.add(new ChatRoomDTO(chatRoom.getId(), chatRoom.getTitle(), activeUsers));
        }

        chatRoomDTOs.sort((a, b) -> {
            int idxA = roomIdsSortedByActiveUsers.indexOf(a.getId().toString());
            int idxB = roomIdsSortedByActiveUsers.indexOf(b.getId().toString());
            return Integer.compare(idxA, idxB);
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
}
