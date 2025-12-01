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

import com.malak.chatapp.domain.Role;
import com.malak.chatapp.domain.User;
import com.malak.chatapp.dto.ApiResponse;
import com.malak.chatapp.dto.CreateUserDto;
import com.malak.chatapp.dto.LoginRequest;
import com.malak.chatapp.secuirty.CustomUserDetails;
import com.malak.chatapp.secuirty.JwtService;
import com.malak.chatapp.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register( @RequestBody CreateUserDto createUserDto) {
	    User createdUser = userService.createUser(createUserDto, Role.USER);
	    
	    // Generate JWT for the new user
	    String token = jwtService.generateToken(createdUser.getUsername(), createdUser.getRole());
	    ApiResponse<String> response = ApiResponse.success(token, "user registered successfully");
	    
	   
	    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest request) throws Exception{
//        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            String jwtToken = jwtService.generateToken(authentication.getName(), userDetails.getUser().getRole());
            ApiResponse<String> response = ApiResponse.success(jwtToken, "login successfully");

            return ResponseEntity.ok(response);     
    }

}
