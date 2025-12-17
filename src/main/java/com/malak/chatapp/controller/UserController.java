package com.malak.chatapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.malak.chatapp.dto.ApiResponse;
import com.malak.chatapp.dto.UserDto;
import com.malak.chatapp.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "User")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<UserDto> users = userService.findAllUsers();
        
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable @NotNull Long userId) {
    	
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(userId), "Users retrieved successfully"));
    }
    
    /**
     * Search users by username (partial match, case-insensitive)
     * GET /api/users/search?query=john
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserDto>>> searchUsers(
            @RequestParam @NotBlank(message = "Query must not be blank") String query) {
        
        List<UserDto> results = userService.searchUsersByUsername(query);
        return ResponseEntity.ok(ApiResponse.success(results,
                String.format("Found %d users matching '%s'", results.size(), query)));
    }
}
