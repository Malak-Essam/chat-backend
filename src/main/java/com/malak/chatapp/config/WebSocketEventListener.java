package com.malak.chatapp.config;

import java.security.Principal;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.malak.chatapp.service.UserService;
import com.malak.chatapp.service.UserStatusService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    
    private final UserStatusService userStatusService;
    private final UserService userService;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        
        if (principal != null) {
            String username = principal.getName();
            String sessionId = headerAccessor.getSessionId();
            
            try {
                // Get user ID from username
                Long userId = userService.findUserByUsername(username).getId();
                
                // Mark user as online
                userStatusService.userConnected(userId, sessionId);
                
                log.info("WebSocket connected - User: {}, Session: {}", username, sessionId);
            } catch (Exception e) {
                log.error("Error handling WebSocket connect for user {}: {}", username, e.getMessage());
            }
        }
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        
        if (principal != null) {
            String username = principal.getName();
            String sessionId = headerAccessor.getSessionId();
            
            try {
                // Get user ID from username
                Long userId = userService.findUserByUsername(username).getId();
                
                // Mark user as offline
                userStatusService.userDisconnected(userId);
                
                log.info("WebSocket disconnected - User: {}, Session: {}", username, sessionId);
            } catch (Exception e) {
                log.error("Error handling WebSocket disconnect for user {}: {}", username, e.getMessage());
            }
        }
    }
}