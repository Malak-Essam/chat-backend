package com.malak.chatapp.secuirty;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.malak.chatapp.dto.ApiResponse;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    private final ObjectMapper objectMapper;
  

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException) throws IOException, JsonProcessingException, java.io.IOException {
        
        // This method is called when authentication fails
        
        // 1. Set response type to JSON
        response.setContentType("application/json;charset=UTF-8");
        
        // 2. Set status to 401 (Unauthorized)
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 3. Create your custom API response
        ApiResponse<Object> apiResponse = ApiResponse.error(
            "Authentication required: Missing or invalid JWT token"
        );

        // 4. Write JSON response manually
        response.getWriter().write(
            objectMapper.writeValueAsString(apiResponse)
        );
    }
}