package com.malak.chatapp.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendRequestDto {
	private Long id;
	private UserDto sender;
    private UserDto receiver;
    private String status;
    private LocalDateTime createdAt;
}
