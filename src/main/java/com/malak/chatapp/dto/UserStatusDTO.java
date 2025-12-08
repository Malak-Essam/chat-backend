package com.malak.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusDTO {
    private Long userId;
    private String username;
    private String status; // "ONLINE" or "OFFLINE"
    private LocalDateTime lastSeen;
}