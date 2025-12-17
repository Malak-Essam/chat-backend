package com.malak.chatapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.malak.chatapp.domain.RefreshToken;
import com.malak.chatapp.domain.Role;
import com.malak.chatapp.domain.User;
import com.malak.chatapp.dto.ApiResponse;
import com.malak.chatapp.dto.CreateUserDto;
import com.malak.chatapp.dto.LoginRequest;
import com.malak.chatapp.dto.LoginResponseDto;
import com.malak.chatapp.dto.RefreshTokenRequest;
import com.malak.chatapp.dto.TokenDto;
import com.malak.chatapp.dto.UserDto;
import com.malak.chatapp.exception.ResourceNotFoundException;
import com.malak.chatapp.secuirty.CustomUserDetails;
import com.malak.chatapp.secuirty.JwtService;
import com.malak.chatapp.service.RefreshTokenService;
import com.malak.chatapp.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private final RefreshTokenService refreshTokenService;

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TokenDto>> register(@Valid @RequestBody CreateUserDto createUserDto) {
	    User createdUser = userService.createUser(createUserDto, Role.USER);
	    
	    // Generate JWT for the new user
	    String access = jwtService.generateAccessToken(createdUser.getUsername(), createdUser.getRole());
	    RefreshToken refreshToken = refreshTokenService.createRefreshToken(createdUser.getUsername(), createdUser.getRole());
        String refresh = refreshToken.getToken();
        TokenDto tokenDto = TokenDto.builder().access(access).refresh(refresh).expiresIn(jwtService.getAccessTokenExpiration()).build();
        ApiResponse<TokenDto> response = ApiResponse.success(tokenDto, "Registered successfully");
	    
	   
	    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequest request) throws Exception{

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            UserDto userDto = UserDto.builder().id(user.getId()).username(user.getUsername()).role(user.getRole()).build();

            String access = jwtService.generateAccessToken(authentication.getName(), userDetails.getUser().getRole());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(authentication.getName(), userDetails.getUser().getRole());
            String refresh = refreshToken.getToken();
            
            TokenDto tokenDto = TokenDto.builder().access(access).refresh(refresh).expiresIn(jwtService.getAccessTokenExpiration()).build();
            LoginResponseDto loginResponseDto = LoginResponseDto.builder().tokenDto(tokenDto).userDto(userDto).build();
            ApiResponse<LoginResponseDto> response = ApiResponse.success(loginResponseDto, "login successfully");

            return ResponseEntity.ok(response);     
    }
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenDto>> refreshToken(@Valid @RequestBody RefreshTokenRequest request){
    	String requestRefreshToken = request.getRefreshToken();
    	RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
    			.orElseThrow(() -> new ResourceNotFoundException("refresh token not found"));
    	
    	if(refreshToken.isRevoked()) {
    		throw new IllegalArgumentException("Refresh token is revoked"); 
    	}
    	if(!refreshTokenService.verifyExpiration(refreshToken)) {
    		throw new IllegalArgumentException("Refresh token is expired");
    	}
    	
    	String username = jwtService.extractUsername(requestRefreshToken);
    	if(! jwtService.isTokenValid(requestRefreshToken, username) || !jwtService.isRefreshToken(requestRefreshToken)) {
    		throw new IllegalArgumentException("Invalid refresh token");
    	}
    	User user = userService.findUserByUsername(username);
    	String newAccessToken = jwtService.generateAccessToken(username, user.getRole());
    	TokenDto tokenDto = TokenDto.builder().access(newAccessToken).refresh(requestRefreshToken).expiresIn(jwtService.getAccessTokenExpiration()).build();
        ApiResponse<TokenDto> response = ApiResponse.success(tokenDto, "refresh the access token successfully");
		
        return ResponseEntity.ok(response);
    	
    	
    }
    

}
