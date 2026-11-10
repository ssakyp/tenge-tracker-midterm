package com.tengetracker.controller;

import com.tengetracker.dto.BudgetStatusResponse;
import com.tengetracker.model.Budget;
import com.tengetracker.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Monthly spending limits per EXPENSE category. Each budget belongs to one category (many-to-one); one budget per category and month/year.")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    @Operation(summary = "List budgets", description = "Optional query parameters month (1-12) and year filter the list.")
    @ApiResponse(responseCode = "200", description = "Budgets retrieved")
    public List<Budget> getAllBudgets(
            @Parameter(description = "Filter by month (1-12)", example = "10") @RequestParam(required = false) Integer month,
            @Parameter(description = "Filter by year", example = "2026") @RequestParam(required = false) Integer year) {
        return budgetService.getAllBudgets(month, year);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a budget by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Budget found"),
            @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    public Budget getBudgetById(@Parameter(description = "Budget ID", example = "1") @PathVariable Long id) {
        return budgetService.getBudgetById(id);
    }

    @PostMapping
    @Operation(summary = "Create a budget", description = "Body: categoryId (existing EXPENSE category), limitAmount (> 0), month (1-12), year (2000-2100).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Budget created"),
            @ApiResponse(responseCode = "400", description = "Validation error or category is not EXPENSE"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Budget already exists for this category and month/year")
    })
    public ResponseEntity<Budget> createBudget(@Valid @RequestBody Budget budget) {
        return new ResponseEntity<>(budgetService.createBudget(budget), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a budget", description = "Replaces categoryId, limitAmount, month and year.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Budget updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Budget or category not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate budget for this category and month/year")
    })
    public Budget updateBudget(@Parameter(description = "Budget ID", example = "1") @PathVariable Long id,
                               @Valid @RequestBody Budget budget) {
        return budgetService.updateBudget(id, budget);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Budget deleted"),
            @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    public ResponseEntity<Void> deleteBudget(@Parameter(description = "Budget ID", example = "1") @PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get budget status", description = "Related data: sums the category's EXPENSE transactions in the budget's month and returns spent, remaining and isExceeded.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status calculated"),
            @ApiResponse(responseCode = "404", description = "Budget not found")
    })
    public BudgetStatusResponse getBudgetStatus(@Parameter(description = "Budget ID", example = "1") @PathVariable Long id) {
        return budgetService.getBudgetStatus(id);
    }
}
