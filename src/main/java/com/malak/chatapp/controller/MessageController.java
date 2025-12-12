package com.malak.chatapp.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.malak.chatapp.domain.Message;
import com.malak.chatapp.domain.User;
import com.malak.chatapp.service.MessageService;
import com.malak.chatapp.service.UserService;

import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/messages")
public class MessageController {
	private final MessageService messageService;
    private final UserService userService;

    public MessageController(MessageService service, UserService userService) {
        this.messageService = service;
        this.userService = userService;
    }

    @GetMapping("/{user1}/{user2}")
    public List<Message> getConversation(
    		@PathVariable 
    		@NotNull(message = "User 1 id must be not null")
    		Long user1,
    		@PathVariable 
    		@NotNull(message = "User 2 id must be not null")                            
    		Long user2) {
        User u1 = userService.findUserById(user1);
        User u2 = userService.findUserById(user2);
        return messageService.getConversation(u1, u2);
    }
}
