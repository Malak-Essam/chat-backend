package com.malak.chatapp.exception;

public class FriendRequestNotFoundException extends ResourceNotFoundException {
    public FriendRequestNotFoundException(Long id) {
        super("Friend request not found with id: " + id);
    }
}