package com.malak.chatapp.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.malak.chatapp.secuirty.CustomUserDetailsService;
import com.malak.chatapp.secuirty.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {
    
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            
            if (token == null || !token.startsWith("Bearer ")) {
                log.warn("WebSocket connection attempt without valid Authorization header");
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }
            
            token = token.substring(7);
            
            try {
                String username = jwtService.extractUsername(token);
                
                if (username == null || !jwtService.isTokenValid(token, username)) {
                    log.warn("Invalid JWT token for WebSocket connection");
                    throw new IllegalArgumentException("Invalid or expired JWT token");
                }
                
                // Load full user details with authorities (consistent with JwtAuthFilter)
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken auth = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities()
                    );
                
                accessor.setUser(auth);
                log.debug("WebSocket authentication successful for user: {}", username);
                
            } catch (Exception e) {
                log.error("WebSocket authentication failed: {}", e.getMessage());
                throw new IllegalArgumentException("Authentication failed: " + e.getMessage());
            }
        }
        
        return message;
    }
}