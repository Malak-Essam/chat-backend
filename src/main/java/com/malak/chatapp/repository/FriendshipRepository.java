package com.malak.chatapp.repository;

import com.malak.chatapp.domain.Friendship;
import com.malak.chatapp.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    
    // Get all friends for a user (returns User objects)
	@Query("""
		    SELECT u FROM User u
		    WHERE u.id IN (
		       SELECT f.user2.id FROM Friendship f WHERE f.user1.id = :userId
		    )
		    OR u.id IN (
		       SELECT f.user1.id FROM Friendship f WHERE f.user2.id = :userId
		    )
		    """)
    List<User> getFriendsForUserId(@Param("userId") Long userId);
    
    // Get all friendship records involving a user
    @Query("SELECT f FROM Friendship f " +
           "WHERE f.user1.id = :userId OR f.user2.id = :userId")
    List<Friendship> findAllByUserId(@Param("userId") Long userId);
    
    // Check if two users are friends
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END " +
           "FROM Friendship f " +
           "WHERE (f.user1.id = :userId1 AND f.user2.id = :userId2) " +
           "OR (f.user1.id = :userId2 AND f.user2.id = :userId1)")
    boolean areFriends(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    // Find friendship between two users
    @Query("SELECT f FROM Friendship f " +
           "WHERE (f.user1.id = :userId1 AND f.user2.id = :userId2) " +
           "OR (f.user1.id = :userId2 AND f.user2.id = :userId1)")
    Optional<Friendship> findByUsers(@Param("userId1") Long userId1, 
                                      @Param("userId2") Long userId2);
    
    // Delete friendship between two users
    @Modifying
    @Query("DELETE FROM Friendship f " +
           "WHERE (f.user1.id = :userId1 AND f.user2.id = :userId2) " +
           "OR (f.user1.id = :userId2 AND f.user2.id = :userId1)")
    void deleteFriendship(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    // Count total friends for a user
    @Query("SELECT COUNT(f) FROM Friendship f " +
           "WHERE f.user1.id = :userId OR f.user2.id = :userId")
    long countFriendsByUserId(@Param("userId") Long userId);
    
    // Get mutual friends between two users
    @Query("SELECT CASE " +
           "  WHEN f1.user1.id = :userId1 THEN f1.user2 " +
           "  ELSE f1.user1 " +
           "END " +
           "FROM Friendship f1 " +
           "WHERE (f1.user1.id = :userId1 OR f1.user2.id = :userId1) " +
           "AND EXISTS (" +
           "  SELECT 1 FROM Friendship f2 " +
           "  WHERE (f2.user1.id = :userId2 OR f2.user2.id = :userId2) " +
           "  AND (" +
           "    (f1.user1.id = f2.user1.id) OR " +
           "    (f1.user1.id = f2.user2.id) OR " +
           "    (f1.user2.id = f2.user1.id) OR " +
           "    (f1.user2.id = f2.user2.id)" +
           "  )" +
           "  AND f1.user1.id != :userId2 " +
           "  AND f1.user2.id != :userId2" +
           ")")
    List<User> findMutualFriends(@Param("userId1") Long userId1, 
                                  @Param("userId2") Long userId2);
    
    // Search friends by username - FIXED: Proper join
    @Query("SELECT u FROM User u " +
           "WHERE u.id IN (" +
           "  SELECT CASE WHEN f.user1.id = :userId THEN f.user2.id ELSE f.user1.id END " +
           "  FROM Friendship f " +
           "  WHERE (f.user1.id = :userId OR f.user2.id = :userId)" +
           ") " +
           "AND LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchFriends(@Param("userId") Long userId, 
                             @Param("searchTerm") String searchTerm);
    
    // Get friend IDs only (more efficient)
    @Query("SELECT CASE WHEN f.user1.id = :userId THEN f.user2.id ELSE f.user1.id END " +
           "FROM Friendship f " +
           "WHERE f.user1.id = :userId OR f.user2.id = :userId")
    List<Long> getFriendIdsForUserId(@Param("userId") Long userId);
}