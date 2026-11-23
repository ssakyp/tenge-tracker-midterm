package com.tengetracker.controller;

import com.tengetracker.dto.patch.CategoryPatchRequest;
import com.tengetracker.model.Budget;
import com.tengetracker.model.Category;
import com.tengetracker.model.Transaction;
import com.tengetracker.security.AuthUser;
import com.tengetracker.service.CategoryService;
import com.tengetracker.service.TransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Shared INCOME/EXPENSE categories. GET list/by-id is public; changes need a token.")
public class CategoryController {

    private final CategoryService categoryService;
    private final TransactionService transactionService;

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "List categories (public)")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "Get a category by ID (public)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public Category getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    @PostMapping
    @Operation(summary = "Create a category", description = "Body: name, type (INCOME or EXPENSE).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        return new ResponseEntity<>(categoryService.createCategory(category), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a category", description = "409 if the type changes while transactions or budgets use it.")
    public Category updateCategory(@PathVariable Long id, @Valid @RequestBody Category category) {
        return categoryService.updateCategory(id, category);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a category", description = "Send only name and/or type.")
    public Category patchCategory(@PathVariable Long id, @Valid @RequestBody CategoryPatchRequest patch) {
        return categoryService.patchCategory(id, patch);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category", description = "409 if transactions or budgets use it.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Category is in use")
    })
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "My transactions in this category")
    public List<Transaction> getCategoryTransactions(@PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return transactionService.getTransactionsByCategory(id, me.id());
    }

    @GetMapping("/{id}/budgets")
    @Operation(summary = "My budgets for this category")
    public List<Budget> getCategoryBudgets(@PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return categoryService.getBudgetsByCategory(id, me.id());
    }
}
