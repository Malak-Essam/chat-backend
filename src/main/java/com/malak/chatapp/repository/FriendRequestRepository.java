package com.malak.chatapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.malak.chatapp.domain.FriendRequest;
import com.malak.chatapp.domain.FriendRequestStatus;

// ========================================
// FriendRequest Repository
// ========================================
@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    
    // Find all pending requests received by a user
    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, FriendRequestStatus status);
    
    // Find all pending requests sent by a user
    List<FriendRequest> findBySenderIdAndStatus(Long senderId, FriendRequestStatus status);
    
    // Find specific request from sender to receiver
    Optional<FriendRequest> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    
    // Find any request between two users (either direction)
    @Query("SELECT fr FROM FriendRequest fr " +
           "WHERE (fr.sender.id = :userId1 AND fr.receiver.id = :userId2) " +
           "OR (fr.sender.id = :userId2 AND fr.receiver.id = :userId1)")
    Optional<FriendRequest> findBetweenUsers(@Param("userId1") Long userId1, 
                                             @Param("userId2") Long userId2);
    
    // Find pending request between two users (either direction)
    @Query("SELECT fr FROM FriendRequest fr " +
           "WHERE ((fr.sender.id = :userId1 AND fr.receiver.id = :userId2) " +
           "OR (fr.sender.id = :userId2 AND fr.receiver.id = :userId1)) " +
           "AND fr.status = 'PENDING'")
    Optional<FriendRequest> findPendingBetweenUsers(@Param("userId1") Long userId1, 
                                                     @Param("userId2") Long userId2);
    
    // Check if request exists between users
    @Query("SELECT CASE WHEN COUNT(fr) > 0 THEN true ELSE false END " +
           "FROM FriendRequest fr " +
           "WHERE (fr.sender.id = :userId1 AND fr.receiver.id = :userId2) " +
           "OR (fr.sender.id = :userId2 AND fr.receiver.id = :userId1)")
    boolean existsBetweenUsers(@Param("userId1") Long userId1, 
                               @Param("userId2") Long userId2);
    
    // Count pending requests for a user
    long countByReceiverIdAndStatus(Long receiverId, FriendRequestStatus status);
    
    // Find all requests for a user (sent or received)
    @Query("SELECT fr FROM FriendRequest fr " +
           "WHERE fr.sender.id = :userId OR fr.receiver.id = :userId")
    List<FriendRequest> findAllByUserId(@Param("userId") Long userId);
    
    // Find requests with pagination
    Page<FriendRequest> findByReceiverIdAndStatus(Long receiverId, 
                                                   FriendRequestStatus status, 
                                                   Pageable pageable);
    
    // Delete old rejected requests (cleanup)
    @Modifying
    @Query("DELETE FROM FriendRequest fr " +
           "WHERE fr.status = 'REJECTED' " +
           "AND fr.updatedAt < :cutoffDate")
    void deleteOldRejectedRequests(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}