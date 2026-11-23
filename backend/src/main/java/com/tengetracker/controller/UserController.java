package com.tengetracker.controller;

import com.tengetracker.dto.auth.AuthResponse;
import com.tengetracker.dto.auth.ChangePasswordRequest;
import com.tengetracker.dto.auth.UpdateProfileRequest;
import com.tengetracker.dto.auth.UpdateUsernameRequest;
import com.tengetracker.dto.auth.UserResponse;
import com.tengetracker.security.AuthUser;
import com.tengetracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
@Tag(name = "Current user", description = "Profile of the logged-in user (identified by the JWT, not by an ID in the URL).")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get current user", description = "Returns the user identified by the Bearer token, with profile and counters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current user"),
            @ApiResponse(responseCode = "401", description = "Missing, invalid or expired token")
    })
    public UserResponse me(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return userService.getCurrentUser(me.id());
    }

    @PatchMapping
    @Operation(summary = "Update username", description = "Changes the username and returns a new token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Username changed"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "409", description = "Username already taken")
    })
    public AuthResponse updateUsername(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
                                       @Valid @RequestBody UpdateUsernameRequest request) {
        return userService.updateUsername(me.id(), request);
    }

    @PutMapping("/password")
    @Operation(summary = "Change password", description = "Requires the current password. All previously issued tokens become invalid; a new token is returned.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed, new token returned"),
            @ApiResponse(responseCode = "400", description = "Current password wrong or new password invalid"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public AuthResponse changePassword(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
                                       @Valid @RequestBody ChangePasswordRequest request) {
        return userService.changePassword(me.id(), request);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update profile", description = "Replaces full name, email, preferred currency and bio (one-to-one UserProfile).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public UserResponse updateProfile(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(me.id(), request);
    }
}
