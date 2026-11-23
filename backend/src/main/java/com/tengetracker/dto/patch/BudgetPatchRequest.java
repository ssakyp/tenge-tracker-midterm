package com.tengetracker.dto.patch;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/** PATCH body: every field is optional; only the fields that are present are changed. */
@Data
public class BudgetPatchRequest {
    private Long categoryId;

    @Positive(message = "Limit amount must be greater than zero")
    private BigDecimal limitAmount;

    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @Min(value = 2000, message = "Year must be between 2000 and 2100")
    @Max(value = 2100, message = "Year must be between 2000 and 2100")
    private Integer year;
}
