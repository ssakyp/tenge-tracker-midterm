package com.tengetracker.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {
    @Schema(example = "aruzhan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be 3-30 characters")
    @Pattern(regexp = "^[A-Za-z0-9_.]+$", message = "Username may contain only letters, digits, '_' and '.'")
    private String username;

    @Schema(example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be 8-72 characters")
    private String password;

    @Schema(example = "Aruzhan Bekova")
    @Size(max = 100, message = "Full name must be at most 100 characters")
    private String fullName;

    @Schema(example = "aruzhan@example.kz")
    @Email(message = "Email must be a valid email address")
    @Size(max = 120, message = "Email must be at most 120 characters")
    private String email;
}
