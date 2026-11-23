package com.tengetracker.dto.patch;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** PATCH body: every field is optional; only the fields that are present are changed. */
@Data
public class CategoryPatchRequest {
    @Pattern(regexp = ".*\\S.*", message = "Category name must not be blank")
    private String name;

    @Pattern(regexp = "^(INCOME|EXPENSE)$", message = "Type must be INCOME or EXPENSE")
    private String type;
}
