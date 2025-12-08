package com.malak.chatapp.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.malak.chatapp.dto.TypingIndicatorDTO;
import com.malak.chatapp.service.TypingIndicatorService;
import com.malak.chatapp.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class StatusWebSocketController {
    
    private final TypingIndicatorService typingIndicatorService;
    private final UserService userService;
    
    /**
     * Handle typing indicator events
     * Client sends: { recipientId: 5, typing: true/false }
     */
    @MessageMapping("/typing")
    public void handleTypingIndicator(@Payload TypingIndicatorDTO dto, Principal principal) {
        String username = principal.getName();
        Long userId = userService.findUserByUsername(username).getId();
        
        if (dto.isTyping()) {
            typingIndicatorService.userStartedTyping(userId, dto.getRecipientId());
        } else {
            typingIndicatorService.userStoppedTyping(userId, dto.getRecipientId());
        }
    }
}