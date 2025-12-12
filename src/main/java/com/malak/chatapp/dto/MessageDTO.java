package com.malak.chatapp.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;

public record MessageDTO(
    Long id,
    @NotBlank(message = "Sender username must be not null or blank")
    String sender,      // Changed from senderId to sender (username)
    @NotBlank(message = "Recipient username must be not null or blank")
    String recipient,   // Changed from receiverId to recipient (username)
    @NotBlank(message = "Message must be not null or blank")
    String content,
    LocalDateTime timestamp // Added timestamp
) {}