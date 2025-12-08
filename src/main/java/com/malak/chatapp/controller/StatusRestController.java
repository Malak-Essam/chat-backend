package com.malak.chatapp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.malak.chatapp.dto.ApiResponse;
import com.malak.chatapp.dto.UserStatusDTO;
import com.malak.chatapp.service.UserStatusService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/status")
@RequiredArgsConstructor
class StatusRestController {
    
    private final UserStatusService userStatusService;
    
    /**
     * Get user status
     * GET /api/status/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserStatusDTO>> getUserStatus(@PathVariable Long userId) {
    	ApiResponse<UserStatusDTO> response = ApiResponse.success(userStatusService.getUserStatus(userId));
    	return ResponseEntity.ok(response);
    }
    
    /**
     * Get all online users (for debugging)
     * GET /api/status/online
     */
    @GetMapping("/online")
    public ResponseEntity<ApiResponse<List<Long>>> getOnlineUsers() {
    	ApiResponse<List<Long>> response = ApiResponse.success(userStatusService.getOnlineUsers());
        return ResponseEntity.ok(response);
    }
}
