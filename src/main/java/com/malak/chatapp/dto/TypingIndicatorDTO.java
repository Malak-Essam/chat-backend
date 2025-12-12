package com.malak.chatapp.dto;

import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "recipientId must be not null")
    private Long recipientId;
    private boolean typing;
}
