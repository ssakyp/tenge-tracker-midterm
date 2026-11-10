package com.tengetracker.controller;

import com.tengetracker.dto.SummaryResponse;
import com.tengetracker.service.SummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
@Tag(name = "Summary", description = "Aggregated income, expenses and total assets.")
public class SummaryController {

    private final SummaryService summaryService;

    @GetMapping
    @Operation(summary = "Get income/expense summary for a date range", description = "Both dates are required (YYYY-MM-DD, inclusive). totalAssets is the sum of all account balances.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary calculated"),
            @ApiResponse(responseCode = "400", description = "Missing/invalid date or startDate after endDate")
    })
    public SummaryResponse getSummary(
            @Parameter(description = "Start date (inclusive)", example = "2026-10-01", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive)", example = "2026-10-31", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return summaryService.getSummary(startDate, endDate);
    }
}
