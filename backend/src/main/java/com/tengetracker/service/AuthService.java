package com.tengetracker.service;

import com.tengetracker.dto.auth.AuthResponse;
import com.tengetracker.dto.auth.LoginRequest;
import com.tengetracker.dto.auth.SignupRequest;
import com.tengetracker.exception.ConflictException;
import com.tengetracker.exception.UnauthorizedException;
import com.tengetracker.model.User;
import com.tengetracker.model.UserProfile;
import com.tengetracker.repository.UserRepository;
import com.tengetracker.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    /** Creates the user (BCrypt-hashed password) with an empty profile and logs them in. */
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String username = request.getUsername().trim();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Username '" + username + "' is already taken");
        }
        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        user.setProfile(UserProfile.builder()
                .fullName(blankToNull(request.getFullName()))
                .email(blankToNull(request.getEmail()))
                .preferredCurrency("KZT")
                .build());
        User saved = userRepository.save(user);
        return buildAuthResponse(saved);
    }

    /** Checks username + password; the same message is used for both failures so usernames cannot be probed. */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(request.getUsername().trim())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
        return buildAuthResponse(user);
    }

    public AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateToken(user))
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .user(userMapper.toResponse(user))
                .build();
    }

    static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
