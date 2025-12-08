package com.malak.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingIndicatorDTO {
    private Long userId;
    private String username;
    private Long recipientId;
    private boolean typing;
}
