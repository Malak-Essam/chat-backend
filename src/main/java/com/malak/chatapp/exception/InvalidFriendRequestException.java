package com.malak.chatapp.exception;

public class InvalidFriendRequestException extends RuntimeException {
    public InvalidFriendRequestException(String message) {
        super(message);
    }
}
