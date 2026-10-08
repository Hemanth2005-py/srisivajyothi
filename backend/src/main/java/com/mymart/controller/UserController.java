package com.mymart.controller;

import com.mymart.dto.ApiResponse;
import com.mymart.dto.UserDto;
import com.mymart.dto.UserProfileUpdateDto;
import com.mymart.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Endpoints for managing customer profile and login events")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserDto>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getCurrentUser()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserDto>> updateMyProfile(@Valid @RequestBody UserProfileUpdateDto dto) {
        String userId = userService.getCurrentUserEntity().getId();
        UserDto updated = userService.updateProfile(userId, dto);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", updated));
    }

    @PostMapping("/me/login-event")
    @Operation(summary = "Record successful login event timestamp")
    public ResponseEntity<ApiResponse<Void>> recordLoginEvent() {
        String userId = userService.getCurrentUserEntity().getId();
        userService.recordLoginEvent(userId);
        return ResponseEntity.ok(ApiResponse.ok("Login event recorded", null));
    }
}
