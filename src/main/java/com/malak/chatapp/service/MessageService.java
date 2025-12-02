package com.malak.chatapp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.malak.chatapp.domain.Message;
import com.malak.chatapp.domain.User;
import com.malak.chatapp.repository.MessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageService {
	private final MessageRepository messageRepository;


    public Message sendMessage(User sender, User receiver, String content) {
        Message msg = new Message();
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setContent(content);
        return messageRepository.save(msg);
    }


    public List<Message> getConversation(User user1, User user2) {
        return messageRepository.findConversation(user1.getId(), user2.getId());
    }


    public List<Message> getMessagesSentBy(User sender) {
        return messageRepository.findBySenderId(sender.getId());
    }


    public List<Message> getMessagesReceivedBy(User receiver) {
        return messageRepository.findByReceiverId(receiver.getId());
    }
}
