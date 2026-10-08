package com.mymart.service;

import com.mymart.dto.UserDto;
import com.mymart.dto.UserProfileUpdateDto;
import com.mymart.exception.BadRequestException;
import com.mymart.exception.ResourceNotFoundException;
import com.mymart.model.User;
import com.mymart.repository.UserRepository;
import com.mymart.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getCurrentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ResourceNotFoundException("No authenticated user found in security context");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));
    }

    public UserDto getCurrentUser() {
        return toDto(getCurrentUserEntity());
    }

    public UserDto getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toDto(user);
    }

    public UserDto updateProfile(String userId, UserProfileUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setFullName(dto.getFullName());
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        user.setUpdatedAt(Instant.now());
        User updated = userRepository.save(user);
        return toDto(updated);
    }

    public void recordLoginEvent(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);
        });
    }

    public List<UserDto> getAllCustomers() {
        return userRepository.findAll().stream()
                .filter(u -> "ROLE_CUSTOMER".equals(u.getRole()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public User registerOrSyncUser(String firebaseUid, String email, String username, String fullName, String phone, String role) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            User existing = userRepository.findByEmailIgnoreCase(email).get();
            if (firebaseUid != null && (existing.getFirebaseUid() == null || existing.getFirebaseUid().isEmpty())) {
                existing.setFirebaseUid(firebaseUid);
            }
            if (fullName != null) existing.setFullName(fullName);
            if (phone != null) existing.setPhone(phone);
            return userRepository.save(existing);
        }

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BadRequestException("Username is already taken: " + username);
        }

        User user = User.builder()
                .firebaseUid(firebaseUid != null ? firebaseUid : "usr_" + System.currentTimeMillis())
                .email(email.toLowerCase())
                .username(username.toLowerCase())
                .fullName(fullName)
                .phone(phone)
                .role(role != null ? role : "ROLE_CUSTOMER")
                .createdAt(Instant.now())
                .lastLoginAt(Instant.now())
                .build();

        return userRepository.save(user);
    }

    public UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firebaseUid(user.getFirebaseUid())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
