package com.tengetracker.controller;

import com.tengetracker.dto.BudgetStatusResponse;
import com.tengetracker.dto.patch.BudgetPatchRequest;
import com.tengetracker.model.Budget;
import com.tengetracker.security.AuthUser;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "The current user's monthly limits per EXPENSE category. One budget per user, category and month.")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    @Operation(summary = "List my budgets", description = "Optional month (1-12) and year filters.")
    public List<Budget> getAllBudgets(@RequestParam(required = false) Integer month,
                                      @RequestParam(required = false) Integer year,
                                      @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return budgetService.getAllBudgets(month, year, me.id());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one of my budgets")
    public Budget getBudgetById(@PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return budgetService.getBudgetById(id, me.id());
    }

    @PostMapping
    @Operation(summary = "Create a budget", description = "Body: categoryId (EXPENSE category), limitAmount (> 0), month (1-12), year (2000-2100).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Budget created"),
            @ApiResponse(responseCode = "400", description = "Validation error or category is not EXPENSE"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "You already have a budget for this category and month")
    })
    public ResponseEntity<Budget> createBudget(@Valid @RequestBody Budget budget,
                                               @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return new ResponseEntity<>(budgetService.createBudget(budget, me.id()), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a budget")
    public Budget updateBudget(@PathVariable Long id, @Valid @RequestBody Budget budget,
                               @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return budgetService.updateBudget(id, budget, me.id());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a budget", description = "e.g. {\"limitAmount\": 90000}")
    public Budget patchBudget(@PathVariable Long id, @Valid @RequestBody BudgetPatchRequest patch,
                              @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return budgetService.patchBudget(id, patch, me.id());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        budgetService.deleteBudget(id, me.id());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Budget status", description = "Spent / remaining / exceeded for the budget's month, from my EXPENSE transactions.")
    public BudgetStatusResponse getBudgetStatus(@PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return budgetService.getBudgetStatus(id, me.id());
    }
}
