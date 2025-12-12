package com.malak.chatapp.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.malak.chatapp.domain.FriendRequest;
import com.malak.chatapp.domain.FriendshipStatus;
import com.malak.chatapp.domain.User;
import com.malak.chatapp.dto.ApiResponse;
import com.malak.chatapp.dto.FriendRequestDto;
import com.malak.chatapp.dto.FriendshipStatusDto;
import com.malak.chatapp.dto.UserDto;
import com.malak.chatapp.mapper.FriendMapper;
import com.malak.chatapp.service.FriendService;
import com.malak.chatapp.service.UserService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Validated
public class FriendController {
    
    private final FriendService friendService;
    private final FriendMapper friendMapper;
    private final UserService userService;
    
    // ========================================
    // FRIEND REQUEST ENDPOINTS
    // ========================================
    
    /**
     * Send a friend request
     * POST /api/friends/request/{receiverId}
     */
    @PostMapping("/request/{receiverId}")
    public ResponseEntity<ApiResponse<FriendRequestDto>> sendFriendRequest(
            @PathVariable
            @NotNull(message = "ReceiverId must be not null")
            Long receiverId,
            Principal principal) {
        
        User sender = userService.findUserByUsername(principal.getName());
        
        FriendRequest request = friendService.sendFriendRequest(sender.getId(), receiverId);
        FriendRequestDto dto = friendMapper.toFriendRequestDto(request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(dto, "Friend request sent successfully"));
    }
    
    /**
     * Accept a friend request
     * POST /api/friends/request/{requestId}/accept
     */
    @PostMapping("/request/{requestId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptFriendRequest(
            @PathVariable
            @NotNull(message = "RequestId must be not null")
            Long requestId,
            Principal principal) {
        
        User user = userService.findUserByUsername(principal.getName());
        friendService.acceptFriendRequest(requestId, user.getId());
        
        return ResponseEntity.ok(ApiResponse.success(null, "Friend request accepted"));
    }
    
    /**
     * Reject a friend request
     * POST /api/friends/request/{requestId}/reject
     */
    @PostMapping("/request/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectFriendRequest(
            @PathVariable 
            @NotNull(message = "RequestId must be not null")
            Long requestId,
            Principal principal) {
        
        User user = userService.findUserByUsername(principal.getName());
        friendService.rejectFriendRequest(requestId, user.getId());
        
        return ResponseEntity.ok(ApiResponse.success(null, "Friend request rejected"));
    }
    
    /**
     * Cancel a friend request (sender cancels)
     * DELETE /api/friends/request/{requestId}
     */
    @DeleteMapping("/request/{requestId}")
    public ResponseEntity<ApiResponse<Void>> cancelFriendRequest(
            @PathVariable 
            @NotNull(message = "RequestId must be not null")
            Long requestId,
            Principal principal) {
        
        User user = userService.findUserByUsername(principal.getName());
        friendService.cancelFriendRequest(requestId, user.getId());
        
        return ResponseEntity.ok(ApiResponse.success(null, "Friend request cancelled"));
    }
    
    /**
     * Get pending friend requests (received)
     * GET /api/friends/requests/pending
     */
    @GetMapping("/requests/pending")
    public ResponseEntity<ApiResponse<List<FriendRequestDto>>> getPendingRequests(Principal principal) {
        
        User user = userService.findUserByUsername(principal.getName());
        List<FriendRequest> requests = friendService.getPendingRequests(user.getId());
        
        List<FriendRequestDto> dtos = requests.stream()
            .map(friendMapper::toFriendRequestDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos, "Pending requests retrieved"));
    }
    
    /**
     * Get sent friend requests
     * GET /api/friends/requests/sent
     */
    @GetMapping("/requests/sent")
    public ResponseEntity<ApiResponse<List<FriendRequestDto>>> getSentRequests(
            Principal principal) {
        
        User user = userService.findUserByUsername(principal.getName());
        List<FriendRequest> requests = friendService.getSentRequests(user.getId());
        
        List<FriendRequestDto> dtos = requests.stream()
            .map(friendMapper::toFriendRequestDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos, "Sent requests retrieved"));
    }
    
    /**
     * Get count of pending requests
     * GET /api/friends/requests/count
     */
    @GetMapping("/requests/count")
    public ResponseEntity<ApiResponse<Long>> getPendingRequestsCount(
            Principal principal) {
        
        User user = userService.findUserByUsername(principal.getName());
        long count = friendService.countPendingRequests(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success(count, "Request count retrieved"));
    }
    
    // ========================================
    // FRIENDSHIP ENDPOINTS
    // ========================================
    
    /**
     * Get all friends
     * GET /api/friends
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getFriends(Principal principal) {
        
        User user = userService.findUserByUsername(principal.getName());
        List<User> friends = friendService.getFriends(user.getId());
        
        List<UserDto> dtos = friends.stream()
            .map(friendMapper::toUserDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos, 
            String.format("Retrieved %d friends", dtos.size())));
    }
    
    /**
     * Get friend count
     * GET /api/friends/count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getFriendCount(
            Principal principal) {
        
        User user = userService.findUserByUsername(principal.getName());
        long count = friendService.getFriendCount(user.getId());
        
        return ResponseEntity.ok(ApiResponse.success(count, "Friend count retrieved"));
    }
    
    /**
     * Search friends by username
     * GET /api/friends/search?query=john
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserDto>>> searchFriends(
            @RequestParam
            @NotBlank(message = "Query must be not null or blank")
            String query,
            Principal principal) {
        
        User user = userService.findUserByUsername(principal.getName());
        List<User> friends = friendService.searchFriends(user.getId(), query);
        
        List<UserDto> dtos = friends.stream()
            .map(friendMapper::toUserDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos, 
            String.format("Found %d friends matching '%s'", dtos.size(), query)));
    }
    
    /**
     * Check if two users are friends
     * GET /api/friends/check/{userId}
     */
    @GetMapping("/check/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> checkFriendship(
            @PathVariable 
            @NotNull(message = "UserId must be not null")
            Long userId,
            Principal principal) {
        
        User currentUser = userService.findUserByUsername(principal.getName());
        boolean areFriends = friendService.areFriends(currentUser.getId(), userId);
        
        return ResponseEntity.ok(ApiResponse.success(areFriends, 
            areFriends ? "Users are friends" : "Users are not friends"));
    }
    
    /**
     * Get friendship status with another user
     * GET /api/friends/status/{userId}
     */
    @GetMapping("/status/{userId}")
    public ResponseEntity<ApiResponse<FriendshipStatusDto>> getFriendshipStatus(
            @PathVariable 
            @NotNull(message = "UserId must be not null")
            Long userId,
            Principal principal) {
        
        User currentUser = userService.findUserByUsername(principal.getName());
        FriendshipStatus status = friendService.getFriendshipStatus(currentUser.getId(), userId);
        
        FriendshipStatusDto dto = FriendshipStatusDto.builder()
            .status(status.name())
            .build();
        
        return ResponseEntity.ok(ApiResponse.success(dto, "Friendship status retrieved"));
    }
    
    /**
     * Get mutual friends with another user
     * GET /api/friends/mutual/{userId}
     */
    @GetMapping("/mutual/{userId}")
    public ResponseEntity<ApiResponse<List<UserDto>>> getMutualFriends(
            @PathVariable 
            @NotNull(message = "UserId must be not null")
            Long userId,
            Principal principal) {
        
        User currentUser = userService.findUserByUsername(principal.getName());
        List<User> mutualFriends = friendService.getMutualFriends(currentUser.getId(), userId);
        
        List<UserDto> dtos = mutualFriends.stream()
            .map(friendMapper::toUserDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(dtos, 
            String.format("Found %d mutual friends", dtos.size())));
    }
    
    /**
     * Remove a friend (unfriend)
     * DELETE /api/friends/{friendId}
     */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<ApiResponse<Void>> removeFriend(
            @PathVariable 
            @NotNull(message = "UserId must be not null")
            Long friendId,
            Principal principal) {
        
        User user = userService.findUserByUsername(principal.getName());
        friendService.removeFriend(user.getId(), friendId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Friend removed successfully"));
    }
}