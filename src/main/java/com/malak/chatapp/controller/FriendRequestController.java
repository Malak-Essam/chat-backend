package com.malak.chatapp.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.malak.chatapp.domain.FriendRequest;
import com.malak.chatapp.domain.User;
import com.malak.chatapp.dto.ApiResponse;
import com.malak.chatapp.dto.FriendRequestDto;
import com.malak.chatapp.mapper.FriendMapper;
import com.malak.chatapp.service.FriendRequestService;
import com.malak.chatapp.service.UserService;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/friend-requests")
@RequiredArgsConstructor
@Validated
public class FriendRequestController {

    private final FriendRequestService friendRequestService;
    private final UserService userService;
    private final FriendMapper friendMapper;

    // ---------------------------
    // Helper
    // ---------------------------
    private User getCurrentUser(Principal principal) {
        return userService.findUserByUsername(principal.getName());
    }

    // ---------------------------
    // Friend Request Endpoints
    // ---------------------------

    @PostMapping("/{receiverId}")
    public ResponseEntity<ApiResponse<FriendRequestDto>> sendFriendRequest(
            @PathVariable @NotNull Long receiverId,
            Principal principal) {

        User sender = getCurrentUser(principal);
        FriendRequest request = friendRequestService.sendFriendRequest(sender.getId(), receiverId);
        FriendRequestDto dto = friendMapper.toFriendRequestDto(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dto, "Friend request sent successfully"));
    }

    @PatchMapping("/{requestId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptFriendRequest(
            @PathVariable @NotNull Long requestId,
            Principal principal) {

        User user = getCurrentUser(principal);
        friendRequestService.acceptFriendRequest(requestId, user.getId());

        return ResponseEntity.ok(ApiResponse.success(null, "Friend request accepted"));
    }

    @PatchMapping("/{requestId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectFriendRequest(
            @PathVariable @NotNull Long requestId,
            Principal principal) {

        User user = getCurrentUser(principal);
        friendRequestService.rejectFriendRequest(requestId, user.getId());

        return ResponseEntity.ok(ApiResponse.success(null, "Friend request rejected"));
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<ApiResponse<Void>> cancelFriendRequest(
            @PathVariable @NotNull Long requestId,
            Principal principal) {

        User user = getCurrentUser(principal);
        friendRequestService.cancelFriendRequest(requestId, user.getId());

        return ResponseEntity.ok(ApiResponse.success(null, "Friend request cancelled"));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<FriendRequestDto>>> getPendingRequests(Principal principal) {
        User user = getCurrentUser(principal);
        List<FriendRequestDto> dtos = friendRequestService.getPendingRequests(user.getId())
                .stream().map(friendMapper::toFriendRequestDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos, "Pending requests retrieved"));
    }

    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<List<FriendRequestDto>>> getSentRequests(Principal principal) {
        User user = getCurrentUser(principal);
        List<FriendRequestDto> dtos = friendRequestService.getSentRequests(user.getId())
                .stream().map(friendMapper::toFriendRequestDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos, "Sent requests retrieved"));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getPendingRequestsCount(Principal principal) {
        User user = getCurrentUser(principal);
        long count = friendRequestService.countPendingRequests(user.getId());
        return ResponseEntity.ok(ApiResponse.success(count, "Request count retrieved"));
    }
}
