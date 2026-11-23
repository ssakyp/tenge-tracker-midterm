package com.tengetracker.controller;

import com.tengetracker.dto.patch.TransactionPatchRequest;
import com.tengetracker.model.Transaction;
import com.tengetracker.security.AuthUser;
import com.tengetracker.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "The current user's income, expenses and transfers. Creating, updating or deleting a transaction adjusts account balances atomically. Transactions can carry tags (many-to-many).")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "List my transactions", description = "Newest first. Optional filters: startDate, endDate (YYYY-MM-DD) and type.")
    public List<Transaction> getAllTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String type,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return transactionService.getAllTransactions(startDate, endDate, type, me.id());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one of my transactions")
    public Transaction getTransactionById(@PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return transactionService.getTransactionById(id, me.id());
    }

    @PostMapping
    @Operation(summary = "Create a transaction", description = "Required: accountId, amount (> 0), type, date. TRANSFER needs targetAccountId (also yours) and no category. Optional tagIds.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created and balances updated"),
            @ApiResponse(responseCode = "400", description = "Validation error, insufficient balance, category mismatch"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Account belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Account, category or tag not found")
    })
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody Transaction transaction,
                                                         @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return new ResponseEntity<>(transactionService.createTransaction(transaction, me.id()), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a transaction", description = "Old balance effect is reversed and the new one applied atomically. Omitted tagIds = no tags.")
    public Transaction updateTransaction(@PathVariable Long id, @Valid @RequestBody Transaction transaction,
                                         @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return transactionService.updateTransaction(id, transaction, me.id());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a transaction", description = "Only the present fields change, e.g. {\"description\":\"Lunch\"} or {\"tagIds\":[1,2]}. Balances are recalculated.")
    public Transaction patchTransaction(@PathVariable Long id, @Valid @RequestBody TransactionPatchRequest patch,
                                        @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return transactionService.patchTransaction(id, patch, me.id());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction", description = "Reverses its effect on account balances.")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        transactionService.deleteTransaction(id, me.id());
        return ResponseEntity.noContent().build();
    }
}
