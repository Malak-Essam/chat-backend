package com.malak.chatapp.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
	
}
