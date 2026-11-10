package com.tengetracker.controller;

import com.tengetracker.model.Transaction;
import com.tengetracker.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Record income, expenses, and transfers. Each transaction belongs to a source account (many-to-one) and optionally to a category (many-to-one). Creating, updating or deleting a transaction adjusts account balances in the same database transaction.")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "List transactions", description = "Newest first. Optional filters: startDate, endDate (YYYY-MM-DD, inclusive) and type (INCOME, EXPENSE, TRANSFER).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid filter value")
    })
    public List<Transaction> getAllTransactions(
            @Parameter(description = "Earliest date (inclusive)", example = "2026-10-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Latest date (inclusive)", example = "2026-10-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "INCOME, EXPENSE or TRANSFER", example = "EXPENSE") @RequestParam(required = false) String type) {
        return transactionService.getAllTransactions(startDate, endDate, type);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a transaction by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public Transaction getTransactionById(@Parameter(description = "Transaction ID", example = "1") @PathVariable Long id) {
        return transactionService.getTransactionById(id);
    }

    @PostMapping
    @Operation(summary = "Create a transaction", description = "INCOME adds amount to accountId. EXPENSE subtracts it if funds suffice. TRANSFER (requires targetAccountId, different from accountId, no category) moves funds. Required: accountId, amount (> 0), type, date. If categoryId is given the category must exist and its type must match the transaction type.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction created and balances updated"),
            @ApiResponse(responseCode = "400", description = "Validation error, insufficient balance, missing transfer target, category type mismatch"),
            @ApiResponse(responseCode = "404", description = "Account, target account or category not found")
    })
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody Transaction transaction) {
        return new ResponseEntity<>(transactionService.createTransaction(transaction), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction", description = "Replaces all fields. The balance effect of the old transaction is reversed and the new one applied atomically; if the new one fails (e.g. insufficient funds) nothing changes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction updated"),
            @ApiResponse(responseCode = "400", description = "Validation or business-rule error"),
            @ApiResponse(responseCode = "404", description = "Transaction, account or category not found")
    })
    public Transaction updateTransaction(@Parameter(description = "Transaction ID", example = "1") @PathVariable Long id,
                                         @Valid @RequestBody Transaction transaction) {
        return transactionService.updateTransaction(id, transaction);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction", description = "Deletes the transaction and reverses its effect on account balance(s).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Transaction deleted"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<Void> deleteTransaction(@Parameter(description = "Transaction ID", example = "1") @PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
