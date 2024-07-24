package com.chat.repository;

import com.chat.model.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatRoomId(String chatRoomId);
}
