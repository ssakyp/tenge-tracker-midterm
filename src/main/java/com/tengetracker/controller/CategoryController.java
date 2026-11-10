package com.tengetracker.controller;

import com.tengetracker.model.Budget;
import com.tengetracker.model.Category;
import com.tengetracker.model.Transaction;
import com.tengetracker.service.CategoryService;
import com.tengetracker.service.TransactionService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Income and expense categories. A category has many transactions and many budgets (one-to-many).")
public class CategoryController {

    private final CategoryService categoryService;
    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "List all categories")
    @ApiResponse(responseCode = "200", description = "Categories retrieved")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a category by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public Category getCategoryById(@Parameter(description = "Category ID", example = "1") @PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    @PostMapping
    @Operation(summary = "Create a category", description = "Body: name (required), type (required, INCOME or EXPENSE).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        return new ResponseEntity<>(categoryService.createCategory(category), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a category", description = "Replaces name and type. The type cannot change while the category is used by transactions or budgets (409).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Type change blocked by existing transactions/budgets")
    })
    public Category updateCategory(@Parameter(description = "Category ID", example = "1") @PathVariable Long id,
                                   @Valid @RequestBody Category category) {
        return categoryService.updateCategory(id, category);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category", description = "Returns 409 if transactions or budgets still reference the category.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Category is in use")
    })
    public ResponseEntity<Void> deleteCategory(@Parameter(description = "Category ID", example = "1") @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "List a category's transactions", description = "Related data (one-to-many: category -> transactions), newest first.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved (empty array if none)"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public List<Transaction> getCategoryTransactions(@Parameter(description = "Category ID", example = "5") @PathVariable Long id) {
        return transactionService.getTransactionsByCategory(id);
    }

    @GetMapping("/{id}/budgets")
    @Operation(summary = "List a category's budgets", description = "Related data (one-to-many: category -> budgets).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Budgets retrieved (empty array if none)"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public List<Budget> getCategoryBudgets(@Parameter(description = "Category ID", example = "5") @PathVariable Long id) {
        return categoryService.getBudgetsByCategory(id);
    }
}
