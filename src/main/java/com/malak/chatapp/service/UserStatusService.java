package com.malak.chatapp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.malak.chatapp.domain.User;
import com.malak.chatapp.dto.UserStatusDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserStatusService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final FriendService friendService;
    private final UserService userService;
    // concurrentHashMap is a data structure we will store the details of onlineUsers, and lastSeen
    // userId -> sessionId
    private final ConcurrentHashMap<Long, String> onlineUsers = new ConcurrentHashMap<>();
    
    // userId -> lastSeen timestamp
    private final ConcurrentHashMap<Long, LocalDateTime> lastSeenMap = new ConcurrentHashMap<>();
    
    /**
     * Mark user as online and notify friends
     */
    public void userConnected(Long userId, String sessionId) {
        onlineUsers.put(userId, sessionId);
        lastSeenMap.put(userId, LocalDateTime.now());
        
        log.info("User {} connected with session {}", userId, sessionId);
        
        // Notify all friends that this user is now online
        notifyFriendsAboutStatus(userId, "ONLINE");
    }
    
    /**
     * Mark user as offline and notify friends
     */
    public void userDisconnected(Long userId) {
        String sessionId = onlineUsers.remove(userId);
        lastSeenMap.put(userId, LocalDateTime.now());
        
        log.info("User {} disconnected (session {})", userId, sessionId);
        
        // Notify all friends that this user is now offline
        notifyFriendsAboutStatus(userId, "OFFLINE");
    }
    
    /**
     * Check if user is online
     */
    public boolean isUserOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }
    
    /**
     * Get last seen timestamp
     */
    public LocalDateTime getLastSeen(Long userId) {
        return lastSeenMap.getOrDefault(userId, null);
    }
    
    /**
     * Get all online users (for debugging)
     */
    public List<Long> getOnlineUsers() {
        return onlineUsers.keySet().stream().toList();
    }
    
    /**
     * Get user status DTO
     */
    public UserStatusDTO getUserStatus(Long userId) {
        User user = userService.findUserById(userId);
        boolean online = isUserOnline(userId);
        
        return UserStatusDTO.builder()
            .userId(userId)
            .username(user.getUsername())
            .status(online ? "ONLINE" : "OFFLINE")
            .lastSeen(online ? null : getLastSeen(userId))
            .build();
    }
    
    /**
     * Notify all friends about user's status change
     */
    private void notifyFriendsAboutStatus(Long userId, String status) {
        try {
            // Get user's friends
            List<Long> friendIds = friendService.getFriendIds(userId);
            User user = userService.findUserById(userId);
            
            UserStatusDTO statusDTO = UserStatusDTO.builder()
                .userId(userId)
                .username(user.getUsername())
                .status(status)
                .lastSeen(status.equals("OFFLINE") ? LocalDateTime.now() : null)
                .build();
            
            // Send status update to each friend
            for (Long friendId : friendIds) {
                if (isUserOnline(friendId)) {
                	User recipient = userService.findUserById(friendId);
                    messagingTemplate.convertAndSendToUser(
                    	recipient.getUsername(),
                        "/queue/status",
                        statusDTO
                    );
                }
            }
            
            log.info("Notified {} friends about user {} status: {}", friendIds.size(), userId, status);
            
        } catch (Exception e) {
            log.error("Error notifying friends about status change for user {}: {}", userId, e.getMessage());
        }
    }
}