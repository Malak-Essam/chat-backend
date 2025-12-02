package com.malak.chatapp.dto;

import java.time.LocalDateTime;

public record MessageDTO(
    Long id,
    String sender,      // Changed from senderId to sender (username)
    String recipient,   // Changed from receiverId to recipient (username)
    String content,
    LocalDateTime timestamp // Added timestamp
) {}