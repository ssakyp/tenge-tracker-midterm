package com.tengetracker.dto.patch;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** PATCH body: every field is optional; only the fields that are present are changed. */
@Data
public class AccountPatchRequest {
    @Pattern(regexp = ".*\\S.*", message = "Account name must not be blank")
    @Size(max = 255, message = "Account name must be at most 255 characters")
    private String name;

    @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency must be a 3-letter code")
    private String currency;
}
