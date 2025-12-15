package com.malak.chatapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

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
	private FriendshipRepository friendshipRepository;
	
	@Mock
	private FriendRequestRepository friendRequestRepository;
	
	@InjectMocks
	private FriendService friendService;
	
	private User sender;
	private User receiver;
	
	@BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);

        receiver = new User();
        receiver.setId(2L);
    }
	
	@Test
	void sendFriendRequest_sameUser_throwsException() {
		assertThrows(IllegalArgumentException.class, () ->
		friendService.sendFriendRequest(1L, 1L));
	}
	
	@Test
	void sendFriendRequest_senderNotFound_throwsException() {
		when(userRepository.findById(1L)).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> 
		friendService.sendFriendRequest(1L, 2L));
	}
	
	@Test
	void sendFriendRequest_receiverNotFound_throwsException() {
		when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> 
		friendService.sendFriendRequest(1L, 2L));
	}
	
	@Test
	void sendFriendRequest_usersAlreadyFriends_throwsException() {
	    when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
	    when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
	    when(friendshipRepository.areFriends(1L, 2L)).thenReturn(true);

	    assertThrows(IllegalStateException.class, () ->
	        friendService.sendFriendRequest(1L, 2L)
	    );
	}
	
	@Test
	void sendFriendRequest_pendingRequestAlreadySent_throwsException() {
		FriendRequest existing = new FriendRequest().builder()
				.sender(sender)
				.receiver(receiver)
				.status(FriendRequestStatus.PENDING)
				.build();
		when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
	    when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
	    when(friendshipRepository.areFriends(1L, 2L)).thenReturn(false);
	    when(friendRequestRepository.findPendingBetweenUsers(1L, 2L))
	        .thenReturn(Optional.of(existing));
	    
	    assertThrows(IllegalStateException.class, () -> 
	    	friendService.sendFriendRequest(1L, 2L)
	    		);
	}
	
	@Test
	void sendFriendRequest_reversePendingRequest_throwsSpecificException() {
		FriendRequest existing = FriendRequest.builder()
				.sender(receiver)
				.receiver(sender)
				.status(FriendRequestStatus.PENDING)
				.build();
		
		when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
		when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
		when(friendshipRepository.areFriends(1L, 2L)).thenReturn(false);
		when(friendRequestRepository.findPendingBetweenUsers(1L, 2L))
		.thenReturn(Optional.of(existing));
		
		IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
		friendService.sendFriendRequest(1L, 2L)
				);
		assertTrue(ex.getMessage().contains("This user has already sent you a friend request. Please accept their request instead."));
	}
	
	@Test
	void sendFriendRequest_validRequest_seccess() {
		when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
		when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
		when(friendshipRepository.areFriends(1L, 2L)).thenReturn(false);
		when(friendRequestRepository.findPendingBetweenUsers(1L, 2L))
		.thenReturn(Optional.empty());
		
		when(friendRequestRepository.save(any(FriendRequest.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
		
		FriendRequest result = friendService.sendFriendRequest(1L, 2L);
		
		assertNotNull(result);
		assertEquals(sender, result.getSender());
		assertEquals(receiver, result.getReceiver());
		assertEquals(FriendRequestStatus.PENDING, result.getStatus());
	}
	
}
