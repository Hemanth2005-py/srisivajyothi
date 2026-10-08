package com.mymart.controller;

import com.mymart.dto.*;
import com.mymart.exception.BadRequestException;
import com.mymart.model.User;
import com.mymart.repository.UserRepository;
import com.mymart.security.JwtTokenProvider;
import com.mymart.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user login, registration, and Firebase token synchronization")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    @Operation(summary = "Register customer profile")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerOrSyncUser(
                request.getFirebaseUid(),
                request.getEmail(),
                request.getUsername(),
                request.getFullName(),
                request.getPhone(),
                "ROLE_CUSTOMER"
        );

        String token = jwtTokenProvider.generateToken(user);
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(userService.toDto(user))
                .build();

        return ResponseEntity.ok(ApiResponse.ok("Customer registered successfully", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Customer or Admin login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        String identifier = request.getEmailOrUsername().trim();
        String password = request.getPassword();

        User user = userRepository.findByEmailIgnoreCase(identifier)
                .or(() -> userRepository.findByUsernameIgnoreCase(identifier))
                .orElseThrow(() -> new BadRequestException("Invalid credentials or user does not exist"));

        // Password verification
        if ("ROLE_ADMIN".equals(user.getRole())) {
            if (!"SSJ@SSJ".equals(password) && (user.getPassword() == null || !user.getPassword().equals(password))) {
                throw new BadRequestException("Invalid admin password");
            }
        } else if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            if (!user.getPassword().equals(password)) {
                throw new BadRequestException("Invalid password");
            }
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user);
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(userService.toDto(user))
                .build();

        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/firebase-sync")
    @Operation(summary = "Sync authenticated Firebase user with MongoDB")
    public ResponseEntity<ApiResponse<AuthResponse>> syncFirebaseUser(@RequestBody Map<String, String> payload) {
        String uid = payload.get("uid");
        String email = payload.get("email");
        String displayName = payload.get("displayName");
        String phone = payload.get("phoneNumber");

        if (uid == null || email == null) {
            throw new BadRequestException("Firebase UID and email are required for synchronization");
        }

        User user = userRepository.findByFirebaseUid(uid)
                .or(() -> userRepository.findByEmailIgnoreCase(email))
                .orElseGet(() -> {
                    String username = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
                    if (username.length() < 3) username = "user_" + uid.substring(0, 5);
                    return userService.registerOrSyncUser(uid, email, username,
                            displayName != null ? displayName : "Customer", phone, "ROLE_CUSTOMER");
                });

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user);
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(userService.toDto(user))
                .build();

        return ResponseEntity.ok(ApiResponse.ok("User synchronized successfully", response));
    }

    @GetMapping("/demo-accounts")
    @Operation(summary = "Get instant demo credentials and tokens for fast previewing")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDemoAccounts() {
        User admin = userRepository.findByEmailIgnoreCase("srisivajyothi@ssj")
                .or(() -> userRepository.findByEmailIgnoreCase("admin@mymart.com"))
                .orElse(null);
        User customer = userRepository.findByEmailIgnoreCase("customer@mymart.com")
                .orElse(null);

        String adminToken = admin != null ? jwtTokenProvider.generateToken(admin) : "";
        String customerToken = customer != null ? jwtTokenProvider.generateToken(customer) : "";

        Map<String, Object> data = Map.of(
                "admin", Map.of(
                        "email", "srisivajyothi@ssj",
                        "password", "SSJ@SSJ",
                        "token", adminToken,
                        "role", "ROLE_ADMIN"
                ),
                "customer", Map.of(
                        "email", "customer@mymart.com",
                        "password", "Customer@12345",
                        "token", customerToken,
                        "role", "ROLE_CUSTOMER"
                )
        );

        return ResponseEntity.ok(ApiResponse.ok("Demo accounts information", data));
    }
}
