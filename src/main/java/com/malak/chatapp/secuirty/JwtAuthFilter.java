package com.malak.chatapp.secuirty;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.malak.chatapp.dto.ApiResponse;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter  {
	private final JwtService jwtService;
	private final CustomUserDetailsService userDetailsService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// 1. Skip WebSocket handshake paths
	    if (request.getServletPath().startsWith("/chat")) {
	        filterChain.doFilter(request, response);
	        return;
	    }

	    // 2. Skip preflight OPTIONS requests
	    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
	        filterChain.doFilter(request, response);
	        return;
	    }
		
		
		try {
			
		final String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
		final String token = authHeader.substring(7);
		final String username = jwtService.extractUsername(token);
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		
		if (jwtService.isTokenValid(token, username)) {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
		}
	}

    filterChain.doFilter(request, response);
	} catch (ExpiredJwtException ex) {
        handleJwtException(response, "JWT token has expired", HttpStatus.UNAUTHORIZED);
    } catch (MalformedJwtException ex) {
        handleJwtException(response, "Invalid JWT token format", HttpStatus.BAD_REQUEST);
    } catch (Exception ex) {
        handleJwtException(response, "Authentication error: " + ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }
}

private void handleJwtException(HttpServletResponse response, String message, HttpStatus status) throws IOException {
    response.setStatus(status.value());
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    ApiResponse<Object> apiResponse = ApiResponse.error(message);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    String jsonResponse = objectMapper.writeValueAsString(apiResponse);
    response.getWriter().write(jsonResponse);
}
	

}
