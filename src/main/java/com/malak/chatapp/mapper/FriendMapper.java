package com.malak.chatapp.mapper;

import org.springframework.stereotype.Component;

import com.malak.chatapp.domain.FriendRequest;
import com.malak.chatapp.domain.User;
import com.malak.chatapp.dto.FriendRequestDto;
import com.malak.chatapp.dto.UserDto;

@Component
public class FriendMapper {
    
    public UserDto toUserDto(User user) {
        if (user == null) return null;
        
        return UserDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .build();
    }
    
    public FriendRequestDto toFriendRequestDto(FriendRequest request) {
        if (request == null) return null;
        
        return FriendRequestDto.builder()
            .id(request.getId())
            .sender(toUserDto(request.getSender()))
            .receiver(toUserDto(request.getReceiver()))
            .status(request.getStatus().name())
            .createdAt(request.getCreatedAt())
            .build();
    }
}