package com.malak.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipStatusDto {
    private String status; // FRIENDS, REQUEST_SENT, REQUEST_RECEIVED, NOT_FRIENDS
    private Long friendRequestId; // If there's a pending request
}
