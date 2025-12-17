package com.malak.chatapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.malak.chatapp.domain.FriendRequest;
import com.malak.chatapp.domain.FriendshipStatus;
import com.malak.chatapp.domain.User;
import com.malak.chatapp.exception.ResourceNotFoundException;
import com.malak.chatapp.repository.FriendRequestRepository;
import com.malak.chatapp.repository.FriendshipRepository;
import com.malak.chatapp.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipService {
	// ========================================
    // FRIENDSHIP OPERATIONS
    // ========================================
	private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    
	@Transactional(readOnly = true)
    public List<User> getFriends(Long userId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return friendshipRepository.getFriendsForUserId(userId);
    }
    
    @Transactional(readOnly = true)
    public List<Long> getFriendIds(Long userId) {
        return friendshipRepository.getFriendIdsForUserId(userId);
    }
    
    @Transactional(readOnly = true)
    public boolean areFriends(Long userId1, Long userId2) {
        return friendshipRepository.areFriends(userId1, userId2);
    }
    
    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        userRepository.findById(friendId)
            .orElseThrow(() -> new ResourceNotFoundException("Friend not found with id: " + friendId));
        
        if (!friendshipRepository.areFriends(userId, friendId)) {
            throw new IllegalStateException("Users are not friends");
        }
        
        friendshipRepository.deleteFriendship(userId, friendId);
        log.info("Friendship removed between users {} and {}", userId, friendId);
    }
    
    @Transactional(readOnly = true)
    public List<User> getMutualFriends(Long userId1, Long userId2) {
        return friendshipRepository.findMutualFriends(userId1, userId2);
    }
    
    @Transactional(readOnly = true)
    public List<User> searchFriends(Long userId, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getFriends(userId);
        }
        return friendshipRepository.searchFriends(userId, searchTerm.trim());
    }
    
    @Transactional(readOnly = true)
    public long getFriendCount(Long userId) {
        return friendshipRepository.countFriendsByUserId(userId);
    }
    
    @Transactional(readOnly = true)
    public FriendshipStatus getFriendshipStatus(Long currentUserId, Long otherUserId) {
        if (friendshipRepository.areFriends(currentUserId, otherUserId)) {
            return FriendshipStatus.FRIENDS;
        }
        
        Optional<FriendRequest> request = friendRequestRepository.findPendingBetweenUsers(currentUserId, otherUserId);
        if (request.isPresent()) {
            if (request.get().getSender().getId().equals(currentUserId)) {
                return FriendshipStatus.REQUEST_SENT;
            } else {
                return FriendshipStatus.REQUEST_RECEIVED;
            }
        }
        
        return FriendshipStatus.NOT_FRIENDS;
    }
}
