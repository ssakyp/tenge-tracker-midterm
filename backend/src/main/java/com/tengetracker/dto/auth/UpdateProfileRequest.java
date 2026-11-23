package com.tengetracker.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Schema(example = "Demo User")
    @Size(max = 100, message = "Full name must be at most 100 characters")
    private String fullName;

    @Schema(example = "demo@tengetracker.kz")
    @Email(message = "Email must be a valid email address")
    @Size(max = 120, message = "Email must be at most 120 characters")
    private String email;

    @Schema(example = "KZT")
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "Preferred currency must be a 3-letter code")
    private String preferredCurrency;

    @Schema(example = "Tracking every tenge since 2026")
    @Size(max = 255, message = "Bio must be at most 255 characters")
    private String bio;
}
