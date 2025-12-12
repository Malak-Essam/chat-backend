package com.malak.chatapp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.malak.chatapp.domain.FriendRequest;
import com.malak.chatapp.domain.FriendRequestStatus;
import com.malak.chatapp.domain.Friendship;
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
public class FriendService {
    
    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    
    // ========================================
    // FRIEND REQUEST OPERATIONS
    // ========================================
    
    @Transactional
    public FriendRequest sendFriendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }
        
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new ResourceNotFoundException("Sender not found with id: " + senderId));
        User receiver = userRepository.findById(receiverId)
            .orElseThrow(() -> new ResourceNotFoundException("Receiver not found with id: " + receiverId));
        
        if (friendshipRepository.areFriends(senderId, receiverId)) {
            throw new IllegalStateException("Users are already friends");
        }
        
        Optional<FriendRequest> existingRequest = friendRequestRepository.findPendingBetweenUsers(senderId, receiverId);
        if (existingRequest.isPresent()) {
            FriendRequest request = existingRequest.get();
            if (request.getSender().getId().equals(receiverId)) {
                throw new IllegalStateException("This user has already sent you a friend request. Please accept their request instead.");
            } else {
                throw new IllegalStateException("Friend request already sent and pending");
            }
        }
        
        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(FriendRequestStatus.PENDING);
        
        try {
            FriendRequest savedRequest = friendRequestRepository.save(request);
            log.info("Friend request sent from user {} to user {}", senderId, receiverId);
            return savedRequest;
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Friend request already exists", e);
        }
    }
    
    @Transactional
    public void acceptFriendRequest(Long requestId, Long userId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Friend request not found with id: " + requestId));
        
        if (!request.getReceiver().getId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to accept this request");
        }
        
        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending (current status: " + request.getStatus() + ")");
        }
        
        request.setStatus(FriendRequestStatus.ACCEPTED);
        friendRequestRepository.save(request);
        
        Friendship friendship = Friendship.create(request.getSender(), request.getReceiver());
        
        try {
            friendshipRepository.save(friendship);
            log.info("Friend request {} accepted. Friendship created between users {} and {}", 
                requestId, request.getSender().getId(), request.getReceiver().getId());
        } catch (DataIntegrityViolationException e) {
            log.warn("Friendship already exists between users {} and {}", 
                request.getSender().getId(), request.getReceiver().getId());
        }
    }
    
    @Transactional
    public void rejectFriendRequest(Long requestId, Long userId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Friend request not found with id: " + requestId));
        
        if (!request.getReceiver().getId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to reject this request");
        }
        
        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending (current status: " + request.getStatus() + ")");
        }
        
        request.setStatus(FriendRequestStatus.REJECTED);
        friendRequestRepository.save(request);
        
        log.info("Friend request {} rejected by user {}", requestId, userId);
    }
    
    @Transactional
    public void cancelFriendRequest(Long requestId, Long userId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Friend request not found with id: " + requestId));
        
        if (!request.getSender().getId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to cancel this request");
        }
        
        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel a request that is not pending");
        }
        
        friendRequestRepository.delete(request);
        log.info("Friend request {} cancelled by sender {}", requestId, userId);
    }
    
    @Transactional(readOnly = true)
    public List<FriendRequest> getPendingRequests(Long userId) {
        return friendRequestRepository.findByReceiverIdAndStatus(userId, FriendRequestStatus.PENDING);
    }
    
    @Transactional(readOnly = true)
    public List<FriendRequest> getSentRequests(Long userId) {
        return friendRequestRepository.findBySenderIdAndStatus(userId, FriendRequestStatus.PENDING);
    }
    
    @Transactional(readOnly = true)
    public long countPendingRequests(Long userId) {
        return friendRequestRepository.countByReceiverIdAndStatus(userId, FriendRequestStatus.PENDING);
    }
    
    // ========================================
    // FRIENDSHIP OPERATIONS
    // ========================================
    
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
    
    @Transactional
    public void cleanupOldRejectedRequests(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        friendRequestRepository.deleteOldRejectedRequests(cutoffDate);
        log.info("Cleaned up rejected friend requests older than {} days", daysOld);
    }
}
