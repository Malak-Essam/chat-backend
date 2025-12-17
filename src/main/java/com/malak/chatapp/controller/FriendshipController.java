package com.malak.chatapp.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.malak.chatapp.domain.FriendshipStatus;
import com.malak.chatapp.domain.User;
import com.malak.chatapp.dto.ApiResponse;
import com.malak.chatapp.dto.FriendshipStatusDto;
import com.malak.chatapp.dto.UserDto;
import com.malak.chatapp.mapper.FriendMapper;
import com.malak.chatapp.service.FriendshipService;
import com.malak.chatapp.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Validated
@Tag(name = "Friendship")
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final UserService userService;
    private final FriendMapper friendMapper;

    private User getCurrentUser(Principal principal) {
        return userService.findUserByUsername(principal.getName());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getFriends(Principal principal) {
        User user = getCurrentUser(principal);
        List<UserDto> dtos = friendshipService.getFriends(user.getId())
                .stream().map(friendMapper::toUserDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos, String.format("Retrieved %d friends", dtos.size())));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getFriendCount(Principal principal) {
        User user = getCurrentUser(principal);
        long count = friendshipService.getFriendCount(user.getId());
        return ResponseEntity.ok(ApiResponse.success(count, "Friend count retrieved"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserDto>>> searchFriends(
            @RequestParam @NotBlank String query,
            Principal principal) {

        User user = getCurrentUser(principal);
        List<UserDto> dtos = friendshipService.searchFriends(user.getId(), query)
                .stream().map(friendMapper::toUserDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos, String.format("Found %d friends matching '%s'", dtos.size(), query)));
    }

    @GetMapping("/check/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> checkFriendship(
            @PathVariable @NotNull Long userId,
            Principal principal) {

        User currentUser = getCurrentUser(principal);
        boolean areFriends = friendshipService.areFriends(currentUser.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success(areFriends, areFriends ? "Users are friends" : "Users are not friends"));
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<ApiResponse<FriendshipStatusDto>> getFriendshipStatus(
            @PathVariable @NotNull Long userId,
            Principal principal) {

        User currentUser = getCurrentUser(principal);
        FriendshipStatus status = friendshipService.getFriendshipStatus(currentUser.getId(), userId);
        FriendshipStatusDto dto = FriendshipStatusDto.builder().status(status.name()).build();
        return ResponseEntity.ok(ApiResponse.success(dto, "Friendship status retrieved"));
    }

    @GetMapping("/mutual/{userId}")
    public ResponseEntity<ApiResponse<List<UserDto>>> getMutualFriends(
            @PathVariable @NotNull Long userId,
            Principal principal) {

        User currentUser = getCurrentUser(principal);
        List<UserDto> dtos = friendshipService.getMutualFriends(currentUser.getId(), userId)
                .stream().map(friendMapper::toUserDto).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(dtos, String.format("Found %d mutual friends", dtos.size())));
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<ApiResponse<Void>> removeFriend(
            @PathVariable @NotNull Long friendId,
            Principal principal) {

        User user = getCurrentUser(principal);
        friendshipService.removeFriend(user.getId(), friendId);
        return ResponseEntity.ok(ApiResponse.success(null, "Friend removed successfully"));
    }
}
