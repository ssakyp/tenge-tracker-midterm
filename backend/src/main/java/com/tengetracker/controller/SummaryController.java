package com.tengetracker.controller;

import com.tengetracker.dto.SummaryResponse;
import com.tengetracker.security.AuthUser;
import com.tengetracker.service.SummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
@Tag(name = "Summary", description = "Aggregated income, expenses and total assets of the current user.")
public class SummaryController {

    private final SummaryService summaryService;

    @GetMapping
    @Operation(summary = "My summary for a date range", description = "Required: startDate, endDate (YYYY-MM-DD, inclusive).")
    public SummaryResponse getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return summaryService.getSummary(startDate, endDate, me.id());
    }
}
