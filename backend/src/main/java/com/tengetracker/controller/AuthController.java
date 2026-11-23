package com.tengetracker.controller;

import com.tengetracker.dto.auth.AuthResponse;
import com.tengetracker.dto.auth.LoginRequest;
import com.tengetracker.dto.auth.SignupRequest;
import com.tengetracker.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@SecurityRequirements // public: no token needed
@Tag(name = "Auth", description = "Public signup and login. Both return a JWT access token to send as 'Authorization: Bearer <token>'.")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "Sign up", description = "Creates a user (password stored as a BCrypt hash) with a profile, and returns a JWT so the user is logged in immediately.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created, token returned"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Username already taken")
    })
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return new ResponseEntity<>(authService.signup(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Log in", description = "Checks username and password and returns a signed JWT access token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged in, token returned"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password")
    })
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
