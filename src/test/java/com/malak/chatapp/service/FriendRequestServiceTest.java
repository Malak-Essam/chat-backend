package com.malak.chatapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
import com.malak.chatapp.domain.User;
import com.malak.chatapp.exception.ResourceNotFoundException;
import com.malak.chatapp.repository.FriendRequestRepository;
import com.malak.chatapp.repository.FriendshipRepository;
import com.malak.chatapp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class FriendRequestServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private FriendRequestService friendRequestService;

    private User sender;
    private User receiver;

    @BeforeEach
    void setUp() {
        sender = User.builder().id(1L).build();
        receiver = User.builder().id(2L).build();
    }

    // ===========================
    // Send friend request tests
    // ===========================
    @Test
    void sendFriendRequest_sameUser_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> friendRequestService.sendFriendRequest(1L, 1L));
    }

    @Test
    void sendFriendRequest_senderNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> friendRequestService.sendFriendRequest(1L, 2L));
    }

    @Test
    void sendFriendRequest_receiverNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> friendRequestService.sendFriendRequest(1L, 2L));
    }

    @Test
    void sendFriendRequest_pendingRequestAlreadySent_throwsException() {
        FriendRequest existing = FriendRequest.builder()
                .sender(sender).receiver(receiver)
                .status(FriendRequestStatus.PENDING).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(friendshipRepository.areFriends(1L, 2L)).thenReturn(false);
        when(friendRequestRepository.findPendingBetweenUsers(1L, 2L))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class,
                () -> friendRequestService.sendFriendRequest(1L, 2L));
    }

    @Test
    void sendFriendRequest_reversePendingRequest_throwsSpecificException() {
        FriendRequest existing = FriendRequest.builder()
                .sender(receiver).receiver(sender)
                .status(FriendRequestStatus.PENDING).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(friendshipRepository.areFriends(1L, 2L)).thenReturn(false);
        when(friendRequestRepository.findPendingBetweenUsers(1L, 2L))
                .thenReturn(Optional.of(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> friendRequestService.sendFriendRequest(1L, 2L));
        assertTrue(ex.getMessage()
                .contains("This user has already sent you a friend request. Please accept their request instead."));
    }

    @Test
    void sendFriendRequest_validRequest_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(friendshipRepository.areFriends(1L, 2L)).thenReturn(false);
        when(friendRequestRepository.findPendingBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(friendRequestRepository.save(any(FriendRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FriendRequest request = friendRequestService.sendFriendRequest(1L, 2L);

        assertNotNull(request);
        assertEquals(sender, request.getSender());
        assertEquals(receiver, request.getReceiver());
        assertEquals(FriendRequestStatus.PENDING, request.getStatus());
    }

    // ===========================
    // Accept friend request tests
    // ===========================
    @Test
    void acceptFriendRequest_notFound_throwsException() {
        when(friendRequestRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> friendRequestService.acceptFriendRequest(1L, receiver.getId()));
    }

    @Test
    void acceptFriendRequest_senderCannotAccept_throwsException() {
        FriendRequest request = FriendRequest.builder()
                .id(1L).sender(sender).receiver(receiver)
                .status(FriendRequestStatus.PENDING).build();

        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        assertThrows(IllegalArgumentException.class,
                () -> friendRequestService.acceptFriendRequest(1L, sender.getId()));
    }

    @Test
    void acceptFriendRequest_notPending_throwsException() {
        FriendRequest request = FriendRequest.builder()
                .id(1L).sender(sender).receiver(receiver)
                .status(FriendRequestStatus.REJECTED).build();

        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        assertThrows(IllegalStateException.class,
                () -> friendRequestService.acceptFriendRequest(1L, receiver.getId()));
    }

    // ===========================
    // Reject friend request tests
    // ===========================
    @Test
    void rejectFriendRequest_notFound_throwsException() {
        when(friendRequestRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> friendRequestService.rejectFriendRequest(1L, receiver.getId()));
    }

    @Test
    void rejectFriendRequest_senderCannotReject_throwsException() {
        FriendRequest request = FriendRequest.builder()
                .id(1L).sender(sender).receiver(receiver)
                .status(FriendRequestStatus.PENDING).build();

        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        assertThrows(IllegalArgumentException.class,
                () -> friendRequestService.rejectFriendRequest(1L, sender.getId()));
    }

    @Test
    void rejectFriendRequest_notPending_throwsException() {
        FriendRequest request = FriendRequest.builder()
                .id(1L).sender(sender).receiver(receiver)
                .status(FriendRequestStatus.REJECTED).build();

        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        assertThrows(IllegalStateException.class,
                () -> friendRequestService.rejectFriendRequest(1L, receiver.getId()));
    }

    @Test
    void rejectFriendRequest_validRequest_marksRejected() {
        FriendRequest request = FriendRequest.builder()
                .id(10L).sender(sender).receiver(receiver)
                .status(FriendRequestStatus.PENDING).build();

        when(friendRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(friendRequestRepository.save(any(FriendRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        friendRequestService.rejectFriendRequest(10L, receiver.getId());

        assertEquals(FriendRequestStatus.REJECTED, request.getStatus());
        verify(friendshipRepository, never()).save(any());
    }

    // ===========================
    // Cancel friend request tests
    // ===========================
    @Test
    void cancelFriendRequest_notFound_throwsException() {
        when(friendRequestRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> friendRequestService.cancelFriendRequest(1L, sender.getId()));
    }

    @Test
    void cancelFriendRequest_receiverCannotCancel_throwsException() {
        FriendRequest request = FriendRequest.builder()
                .id(1L).sender(sender).receiver(receiver)
                .status(FriendRequestStatus.PENDING).build();

        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        assertThrows(IllegalArgumentException.class,
                () -> friendRequestService.cancelFriendRequest(1L, receiver.getId()));
    }

    @Test
    void cancelFriendRequest_notPending_throwsException() {
        FriendRequest request = FriendRequest.builder()
                .id(1L).sender(sender).receiver(receiver)
                .status(FriendRequestStatus.REJECTED).build();

        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        assertThrows(IllegalStateException.class,
                () -> friendRequestService.cancelFriendRequest(1L, sender.getId()));
    }

    @Test
    void cancelFriendRequest_validRequest_deletesRequest() {
        FriendRequest request = FriendRequest.builder()
                .id(1L).sender(sender).receiver(receiver)
                .status(FriendRequestStatus.PENDING).build();

        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        friendRequestService.cancelFriendRequest(1L, sender.getId());

        verify(friendRequestRepository).delete(request);
    }
}
