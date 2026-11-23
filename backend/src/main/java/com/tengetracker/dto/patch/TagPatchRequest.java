package com.tengetracker.dto.patch;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** PATCH body: every field is optional; only the fields that are present are changed. */
@Data
public class TagPatchRequest {
    @Pattern(regexp = ".*\\S.*", message = "Tag name must not be blank")
    @Size(max = 30, message = "Tag name must be at most 30 characters")
    private String name;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a hex value like #2563eb")
    private String color;
}
