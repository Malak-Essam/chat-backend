// 1. UPDATED ChatController
package com.malak.chatapp.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.malak.chatapp.domain.Message;
import com.malak.chatapp.dto.MessageDTO;
import com.malak.chatapp.service.MessageService;
import com.malak.chatapp.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final UserService userService;
    
    @MessageMapping("/private")
    public void sendPrivateMessage(@Payload MessageDTO dto, Principal principal) {
    	try {
        // Get sender from JWT authentication (more secure)
        String senderUsername = principal.getName();
        
        // Save message to database
        Message saved = messageService.sendMessage(
            userService.findUserByUsername(senderUsername),
            userService.findUserByUsername(dto.recipient()), // Use recipient username
            dto.content()
        );
        
        // Create response DTO with timestamp
        MessageDTO outgoing = new MessageDTO(
            saved.getId(),
            senderUsername,
            dto.recipient(),
            saved.getContent(),
            saved.getCreatedAt() // Add timestamp
        );
        
        // Send to recipient's private queue
        messagingTemplate.convertAndSendToUser(
            dto.recipient(),
            "/queue/private",
            outgoing
        );
        
        // Send back to sender (so they see their own message)
        messagingTemplate.convertAndSendToUser(
            senderUsername,
            "/queue/private",
            outgoing
        );
    	} catch (Exception e) {
			throw e;
		}
    }
}