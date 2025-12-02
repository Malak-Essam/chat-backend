package com.malak.chatapp.config;

import java.util.ArrayList;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import com.malak.chatapp.secuirty.JwtService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Get token from header
            String token = accessor.getFirstNativeHeader("Authorization");
            
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                
                try {
                    String username = jwtService.extractUsername(token);
                    
                    if (username != null && jwtService.isTokenValid(token, username)) {
                        // Create authentication
                        UsernamePasswordAuthenticationToken auth = 
                            new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
                        
                        accessor.setUser(auth);
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid JWT token: " + e.getMessage());
                }
            } else {
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }
        }
        
        return message;
    }
}
