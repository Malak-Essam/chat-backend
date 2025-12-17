package com.malak.chatapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.malak.chatapp.domain.FriendRequest;
import com.malak.chatapp.domain.FriendRequestStatus;
import com.malak.chatapp.domain.FriendshipStatus;
import com.malak.chatapp.domain.User;
import com.malak.chatapp.exception.ResourceNotFoundException;
import com.malak.chatapp.repository.FriendRequestRepository;
import com.malak.chatapp.repository.FriendshipRepository;
import com.malak.chatapp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class FriendshipServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @InjectMocks
    private FriendshipService friendshipService;

    private User user;
    private User friend;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
        friend = User.builder().id(2L).build();
    }

    // ===========================
    // Remove friend tests
    // ===========================
    @Test
    void removeFriend_userNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> friendshipService.removeFriend(1L, 2L));
    }

    @Test
    void removeFriend_friendNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> friendshipService.removeFriend(1L, 2L));
    }

    @Test
    void removeFriend_notFriends_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendshipRepository.areFriends(1L, 2L)).thenReturn(false);
        assertThrows(IllegalStateException.class,
                () -> friendshipService.removeFriend(1L, 2L));
    }

    @Test
    void removeFriend_validCall_deletesFriendship() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendshipRepository.areFriends(1L, 2L)).thenReturn(true);

        friendshipService.removeFriend(1L, 2L);

        verify(friendshipRepository).deleteFriendship(1L, 2L);
    }

    // ===========================
    // Friendship status tests
    // ===========================
    @Test
    void getFriendshipStatus_notFriends_ReturnNotFriendStatus() {
        when(friendshipRepository.areFriends(1L, 2L)).thenReturn(false);
        when(friendRequestRepository.findPendingBetweenUsers(1L, 2L))
                .thenReturn(Optional.empty());

        FriendshipStatus status = friendshipService.getFriendshipStatus(1L, 2L);
        assertEquals(FriendshipStatus.NOT_FRIENDS, status);
    }

    @Test
    void getFriendshipStatus_areFriends_ReturnFriendsStatus() {
        when(friendshipRepository.areFriends(1L, 2L)).thenReturn(true);
        FriendshipStatus status = friendshipService.getFriendshipStatus(1L, 2L);
        assertEquals(FriendshipStatus.FRIENDS, status);
    }

    @Test
    void getFriendshipStatus_requestSent_ReturnRequestSentStatus() {
        FriendRequest request = FriendRequest.builder()
                .id(1L)
                .sender(user)
                .receiver(friend)
                .status(FriendRequestStatus.PENDING)
                .build();

        when(friendshipRepository.areFriends(1L, 2L)).thenReturn(false);
        when(friendRequestRepository.findPendingBetweenUsers(1L, 2L))
                .thenReturn(Optional.of(request));

        FriendshipStatus status = friendshipService.getFriendshipStatus(1L, 2L);
        assertEquals(FriendshipStatus.REQUEST_SENT, status);
    }

    @Test
    void getFriendshipStatus_requestReceived_ReturnRequestReceivedStatus() {
        FriendRequest request = FriendRequest.builder()
                .id(1L)
                .sender(user)
                .receiver(friend)
                .status(FriendRequestStatus.PENDING)
                .build();

        when(friendshipRepository.areFriends(2L, 1L)).thenReturn(false);
        when(friendRequestRepository.findPendingBetweenUsers(2L, 1L))
                .thenReturn(Optional.of(request));

        FriendshipStatus status = friendshipService.getFriendshipStatus(2L, 1L);
        assertEquals(FriendshipStatus.REQUEST_RECEIVED, status);
    }
}
