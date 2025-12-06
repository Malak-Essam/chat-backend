package com.malak.chatapp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.malak.chatapp.domain.FriendRequest;
import com.malak.chatapp.domain.FriendRequestStatus;
import com.malak.chatapp.domain.User;
import com.malak.chatapp.dto.FriendRequestDto;
import com.malak.chatapp.repository.FriendRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendRequestService {
	private final FriendRequestRepository friendRequestRepository;
	private final UserService userService;
	
	@Transactional
	public FriendRequest SendFriendRequest(FriendRequestDto friendRequestDto) {
		FriendRequest friendRequest = new FriendRequest();
		User sender = userService.findUserById(friendRequestDto.getSender_id());
		User receiver = userService.findUserById(friendRequestDto.getReceiver_id());
		friendRequest.setSender(sender);
		friendRequest.setReceiver(receiver);
		friendRequest.setStatus(FriendRequestStatus.PENDING);
		
		return friendRequestRepository.save(friendRequest);
		 
	}
	
}
