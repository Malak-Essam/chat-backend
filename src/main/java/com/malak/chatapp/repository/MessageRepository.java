package com.malak.chatapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.malak.chatapp.domain.Message;

public interface MessageRepository extends JpaRepository<Message, Long>{
	
	List<Message> findBySenderId(Long senderId);

    List<Message> findByReceiverId(Long receiverId);

    @Query("""
            SELECT m FROM Message m 
            WHERE (m.sender.id = :user1 AND m.receiver.id = :user2)
               OR (m.sender.id = :user2 AND m.receiver.id = :user1)
            ORDER BY m.id ASC
            """)
    List<Message> findConversation(Long user1, Long user2);

}
