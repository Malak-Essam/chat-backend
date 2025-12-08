package com.malak.chatapp.service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.malak.chatapp.domain.User;
import com.malak.chatapp.dto.TypingIndicatorDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TypingIndicatorService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    
    // Key: "userId_recipientId", Value: expiration timestamp
    private final ConcurrentHashMap<String, LocalDateTime> typingUsers = new ConcurrentHashMap<>();
    
    private static final int TYPING_TIMEOUT_SECONDS = 5;
    
    /**
     * User started typing
     */
    public void userStartedTyping(Long userId, Long recipientId) {
        String key = createKey(userId, recipientId);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(TYPING_TIMEOUT_SECONDS);
        
        typingUsers.put(key, expiresAt);
        
        // Notify recipient
        sendTypingIndicator(userId, recipientId, true);
        
        log.debug("User {} started typing to user {}", userId, recipientId);
    }
    
    /**
     * User stopped typing
     */
    public void userStoppedTyping(Long userId, Long recipientId) {
        String key = createKey(userId, recipientId);
        typingUsers.remove(key);
        
        // Notify recipient
        sendTypingIndicator(userId, recipientId, false);
        
        log.debug("User {} stopped typing to user {}", userId, recipientId);
    }
    
    /**
     * Send typing indicator to recipient
     */
    private void sendTypingIndicator(Long userId, Long recipientId, boolean typing) {
        try {
            User user = userService.findUserById(userId);
            User recipient = userService.findUserById(recipientId);
            
            TypingIndicatorDTO dto = TypingIndicatorDTO.builder()
                .userId(userId)
                .username(user.getUsername())
                .recipientId(recipientId)
                .typing(typing)
                .build();
            
            messagingTemplate.convertAndSendToUser(
                recipient.getUsername(),
                "/queue/typing",
                dto
            );
            
        } catch (Exception e) {
            log.error("Error sending typing indicator: {}", e.getMessage());
        }
    }
    /**
     * Cleanup expired typing indicators (runs every 3 seconds)
     */
    @Scheduled(fixedRate = 3000)
    public void cleanupExpiredTypingIndicators() {
        LocalDateTime now = LocalDateTime.now();
        
        typingUsers.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isBefore(now);
            
            if (expired) {
                // Parse key and send stop typing indicator
                String[] parts = entry.getKey().split("_");
                Long userId = Long.parseLong(parts[0]);
                Long recipientId = Long.parseLong(parts[1]);
                
                sendTypingIndicator(userId, recipientId, false);
                log.debug("Auto-stopped typing indicator for user {} to user {}", userId, recipientId);
            }
            
            return expired;
        });
    }
    
    /**
     * Check if user is typing to recipient
     */
    public boolean isUserTyping(Long userId, Long recipientId) {
        String key = createKey(userId, recipientId);
        LocalDateTime expiresAt = typingUsers.get(key);
        
        if (expiresAt == null) {
            return false;
        }
        
        return expiresAt.isAfter(LocalDateTime.now());
    }
    
    private String createKey(Long userId, Long recipientId) {
        return userId + "_" + recipientId;
    }
}