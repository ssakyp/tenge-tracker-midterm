package com.tengetracker.dto.patch;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * PATCH body: every field is optional; only the fields that are present are changed.
 * Balance effects are recalculated exactly like a PUT. To remove the category use PUT.
 */
@Data
public class TransactionPatchRequest {
    private Long accountId;
    private Long categoryId;

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @Pattern(regexp = "^(INCOME|EXPENSE|TRANSFER)$", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Type must be INCOME, EXPENSE or TRANSFER")
    private String type;

    private Long targetAccountId;
    private String description;
    private LocalDate date;
    /** Replaces the tag list when present (send [] to remove all tags). */
    private List<Long> tagIds;
}
