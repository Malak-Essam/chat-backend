package com.malak.chatapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import com.malak.chatapp.domain.Friendship;
import com.malak.chatapp.domain.FriendshipStatus;
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
		assertThrows(IllegalArgumentException.class, () -> friendService.sendFriendRequest(1L, 1L));
	}

	@Test
	void sendFriendRequest_senderNotFound_throwsException() {
		when(userRepository.findById(1L)).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> friendService.sendFriendRequest(1L, 2L));
	}

	@Test
	void sendFriendRequest_receiverNotFound_throwsException() {
		when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> friendService.sendFriendRequest(1L, 2L));
	}

	@Test
	void sendFriendRequest_usersAlreadyFriends_throwsException() {
		when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
		when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
		when(friendshipRepository.areFriends(1L, 2L)).thenReturn(true);

		assertThrows(IllegalStateException.class, () -> friendService.sendFriendRequest(1L, 2L));
	}

	@Test
	void sendFriendRequest_pendingRequestAlreadySent_throwsException() {
		FriendRequest existing = new FriendRequest().builder().sender(sender).receiver(receiver)
				.status(FriendRequestStatus.PENDING).build();
		when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
		when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
		when(friendshipRepository.areFriends(1L, 2L)).thenReturn(false);
		when(friendRequestRepository.findPendingBetweenUsers(1L, 2L)).thenReturn(Optional.of(existing));

		assertThrows(IllegalStateException.class, () -> friendService.sendFriendRequest(1L, 2L));
	}

	@Test
	void sendFriendRequest_reversePendingRequest_throwsSpecificException() {
		FriendRequest existing = FriendRequest.builder().sender(receiver).receiver(sender)
				.status(FriendRequestStatus.PENDING).build();

		when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
		when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
		when(friendshipRepository.areFriends(1L, 2L)).thenReturn(false);
		when(friendRequestRepository.findPendingBetweenUsers(1L, 2L)).thenReturn(Optional.of(existing));

		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> friendService.sendFriendRequest(1L, 2L));
		assertTrue(ex.getMessage()
				.contains("This user has already sent you a friend request. Please accept their request instead."));
	}

	@Test
	void sendFriendRequest_validRequest_seccess() {
		when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
		when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
		when(friendshipRepository.areFriends(1L, 2L)).thenReturn(false);
		when(friendRequestRepository.findPendingBetweenUsers(1L, 2L)).thenReturn(Optional.empty());

		when(friendRequestRepository.save(any(FriendRequest.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		FriendRequest result = friendService.sendFriendRequest(1L, 2L);

		assertNotNull(result);
		assertEquals(sender, result.getSender());
		assertEquals(receiver, result.getReceiver());
		assertEquals(FriendRequestStatus.PENDING, result.getStatus());
	}

//	test accepts
	@Test
	void acceptFriendRequest_senderCannotAccept_throwsException() {
		FriendRequest request = FriendRequest.builder().id(1L).sender(sender).receiver(receiver)
				.status(FriendRequestStatus.PENDING).build();

		when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(request));

		assertThrows(IllegalArgumentException.class, () -> friendService.acceptFriendRequest(1L, sender.getId()));

	}

	@Test
	void acceptFriendRequest_notFound_throwsException() {
		when(friendRequestRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> friendService.acceptFriendRequest(1L, receiver.getId()));
	}

	@Test
	void acceptFriendRequest_notPending_throwsException() {
		FriendRequest request = FriendRequest.builder().id(1L).sender(sender).receiver(receiver)
				.status(FriendRequestStatus.REJECTED).build();
		when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(request));

		assertThrows(IllegalStateException.class, () -> friendService.acceptFriendRequest(1L, receiver.getId()));
	}

	@Test
	void acceptFriendRequest_validRequest_createsFriendship() {
		FriendRequest request = FriendRequest.builder().id(1L).sender(sender).receiver(receiver)
				.status(FriendRequestStatus.PENDING).build();
		when(friendRequestRepository.findById(10L)).thenReturn(Optional.of(request));

		when(friendRequestRepository.save(any(FriendRequest.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		when(friendshipRepository.save(any(Friendship.class))).thenAnswer(invocation -> invocation.getArgument(0));

		friendService.acceptFriendRequest(10L, receiver.getId());

		assertEquals(FriendRequestStatus.ACCEPTED, request.getStatus());

		verify(friendshipRepository).save(argThat(
				friendship -> friendship.getUser1().getId().equals(1L) && friendship.getUser2().getId().equals(2L)));

	}

	@Test
	void rejectFriendRequest_notFound_throwsException() {
		when(friendRequestRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> friendService.rejectFriendRequest(1L, receiver.getId()));
	}

//	any one else than Receiver that call reject request will throws an exception
	@Test
	void rejectFriendRequest_senderCannotAccept_throwsException() {
		FriendRequest request = FriendRequest.builder().id(1L).sender(sender).receiver(receiver)
				.status(FriendRequestStatus.PENDING).build();
		when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(request));

		assertThrows(IllegalArgumentException.class, () -> friendService.rejectFriendRequest(1L, sender.getId()));
	}
	
	@Test
	void rejectFriendRequest_requestStatesNotPending_throwsException() {
		FriendRequest request = FriendRequest.builder().id(1L).sender(sender).receiver(receiver)
				.status(FriendRequestStatus.REJECTED).build();
		when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(request));

		assertThrows(IllegalStateException.class, () -> friendService.rejectFriendRequest(1L, receiver.getId()));
	}
	
	@Test
	void rejectFriendRequest_validRequest_marksRejected() {
		FriendRequest request = new FriendRequest();
		request.setId(10L);
		request.setSender(sender);
		request.setReceiver(receiver);
		request.setStatus(FriendRequestStatus.PENDING);

		when(friendRequestRepository.findById(10L)).thenReturn(Optional.of(request));

		when(friendRequestRepository.save(any(FriendRequest.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		friendService.rejectFriendRequest(10L, receiver.getId());

		assertEquals(FriendRequestStatus.REJECTED, request.getStatus());
		verify(friendshipRepository, never()).save(any());
	}
	
	@Test
	void cancelFriendRequest_notFound_throwsException() {
		when(friendRequestRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> friendService.cancelFriendRequest(1L, receiver.getId()));
	}
	
//	any one else than Sender call cancel request will throws an exception
	@Test
	void cancelFriendRequest_ReceiverCanNotCancel_throwsException() {
		FriendRequest request = FriendRequest.builder().id(1L).sender(sender).receiver(receiver)
				.status(FriendRequestStatus.PENDING).build();
		when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(request));

		assertThrows(IllegalArgumentException.class, () -> friendService.cancelFriendRequest(1L, receiver.getId()));
	}
	
	@Test
	void cancelFriendRequest_requestStatesNotPending_throwsException() {
		FriendRequest request = FriendRequest.builder().id(1L).sender(sender).receiver(receiver)
				.status(FriendRequestStatus.REJECTED).build();
		when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(request));

		assertThrows(IllegalStateException.class, () -> friendService.cancelFriendRequest(1L, sender.getId()));
	}
	@Test
	void cancelFriendRequest_validRequest_deleteRequest() {
		FriendRequest request = FriendRequest.builder().id(1L).sender(sender).receiver(receiver)
				.status(FriendRequestStatus.PENDING).build();
		when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(request));

		friendService.cancelFriendRequest(1L, sender.getId());
		verify(friendRequestRepository).delete(request);
	}
	
	@Test
	void removeFriend_userNotFound_throwsException() {
		when(userRepository.findById(1L)).thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class,() ->
		friendService.removeFriend(1L, 2L));
	}
	
	@Test
	void removeFriend_friendNotFound_throwsException() {
		User user = User.builder().id(1L).build();
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.findById(2L)).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class,() ->
		friendService.removeFriend(1L, 2L));
	}
	@Test
	void removeFriend_notFriends_throwsException() {
		User user = User.builder().id(1L).build();
		User friend = User.builder().id(2L).build();
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
		when(friendshipRepository.areFriends(1L, 2L)).thenReturn(false);
		
		assertThrows(IllegalStateException.class,() ->
		friendService.removeFriend(1L, 2L));
	}
	@Test
	void removeFriend_validCall_delteFriendship() {
		User user = User.builder().id(1L).build();
		User friend = User.builder().id(2L).build();
		
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
		when(friendshipRepository.areFriends(1L, 2L)).thenReturn(true);
		
		friendService.removeFriend(1L, 2L);
		
		verify(friendshipRepository).deleteFriendship(1L, 2L);
	}
	@Test
	void getFriendshipStatus_notFriends_ReturnNotFriendStatus() {
		when(friendshipRepository.areFriends(1L, 2L)).thenReturn(false);
		when(friendRequestRepository.findPendingBetweenUsers(1L, 2L))
        .thenReturn(Optional.empty());
		FriendshipStatus response = friendService.getFriendshipStatus(1L, 2L);
		assertEquals(FriendshipStatus.NOT_FRIENDS, response);
	}
	
	@Test
	void getFriendshipStatus_areFriends_ReturnFriendsStatus() {
		when(friendshipRepository.areFriends(1L, 2L)).thenReturn(true);
		FriendshipStatus response = friendService.getFriendshipStatus(1L, 2L);
		assertEquals(FriendshipStatus.FRIENDS, response);
	}
	
	@Test
	void getFriendshipStatus_REQUEST_SENT_ReturnREQUEST_SENTStatus() {
		FriendRequest request = FriendRequest.builder().id(1L).sender(sender).receiver(receiver).status(FriendRequestStatus.PENDING).build();
		when(friendshipRepository.areFriends(1L, 2L)).thenReturn(false);
		when(friendRequestRepository.findPendingBetweenUsers(1L, 2L)).thenReturn(Optional.of(request));
		FriendshipStatus response = friendService.getFriendshipStatus(1L, 2L);
		assertEquals(FriendshipStatus.REQUEST_SENT, response);
	}
	
	@Test
	void getFriendshipStatus_REQUEST_RECEIVED_ReturnREQUEST_RECEIVEDStatus() {
		FriendRequest request = FriendRequest.builder().id(1L).sender(sender).receiver(receiver).status(FriendRequestStatus.PENDING).build();
		when(friendshipRepository.areFriends(2L, 1L)).thenReturn(false);
		when(friendRequestRepository.findPendingBetweenUsers(2L, 1L)).thenReturn(Optional.of(request));
		FriendshipStatus response = friendService.getFriendshipStatus(2L, 1L);
		assertEquals(FriendshipStatus.REQUEST_RECEIVED, response);
	}
}
