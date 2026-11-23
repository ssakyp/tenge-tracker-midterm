package com.tengetracker.service;

import com.tengetracker.dto.auth.AuthResponse;
import com.tengetracker.dto.auth.ChangePasswordRequest;
import com.tengetracker.dto.auth.UpdateProfileRequest;
import com.tengetracker.dto.auth.UpdateUsernameRequest;
import com.tengetracker.dto.auth.UserResponse;
import com.tengetracker.exception.BadRequestException;
import com.tengetracker.exception.ConflictException;
import com.tengetracker.exception.ResourceNotFoundException;
import com.tengetracker.model.User;
import com.tengetracker.model.UserProfile;
import com.tengetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthService authService;

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        return userMapper.toResponse(getUser(userId));
    }

    /** Changes the username and returns a fresh token (the token carries the username claim). */
    @Transactional
    public AuthResponse updateUsername(Long userId, UpdateUsernameRequest request) {
        User user = getUser(userId);
        String newUsername = request.getUsername().trim();
        userRepository.findByUsernameIgnoreCase(newUsername)
                .filter(other -> !other.getId().equals(userId))
                .ifPresent(other -> {
                    throw new ConflictException("Username '" + newUsername + "' is already taken");
                });
        user.setUsername(newUsername);
        return authService.buildAuthResponse(userRepository.save(user));
    }

    /**
     * Verifies the current password, stores the new BCrypt hash and bumps tokenVersion,
     * which invalidates every token issued before (other devices are logged out).
     * A new token is returned so the current session continues.
     */
    @Transactional
    public AuthResponse changePassword(Long userId, ChangePasswordRequest request) {
        User user = getUser(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            // 400, not 401: the user IS authenticated, the form value is just wrong
            throw new BadRequestException("Current password is incorrect");
        }
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("New password must be different from the current password");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        return authService.buildAuthResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUser(userId);
        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            user.setProfile(profile);
        }
        profile.setFullName(AuthService.blankToNull(request.getFullName()));
        profile.setEmail(AuthService.blankToNull(request.getEmail()));
        profile.setPreferredCurrency(request.getPreferredCurrency() == null
                ? "KZT" : request.getPreferredCurrency().toUpperCase());
        profile.setBio(AuthService.blankToNull(request.getBio()));
        return userMapper.toResponse(userRepository.save(user));
    }
}
