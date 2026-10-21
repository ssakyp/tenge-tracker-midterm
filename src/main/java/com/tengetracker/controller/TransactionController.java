package com.tengetracker.controller;

import com.tengetracker.model.Transaction;
import com.tengetracker.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Record income, expenses, and transfers between accounts. Creating a transaction changes the associated account balance in the same database transaction. Currently this API supports listing and creating transactions only.")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "List all transactions", description = "Returns all transactions stored in the database. Each transaction includes account IDs, amount, type, date, and optional description/category/target account. Amounts must be positive. Dates use ISO format YYYY-MM-DD.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Transaction.class), examples = @ExampleObject(value = "[{\"id\":1,\"accountId\":1,\"categoryId\":null,\"amount\":5000.00,\"type\":\"INCOME\",\"targetAccountId\":null,\"description\":\"Salary\",\"date\":\"2026-10-01\"}]")))
    })
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @PostMapping
    @Operation(summary = "Create a transaction", description = "Creates an INCOME, EXPENSE, or TRANSFER transaction and updates account balance(s). INCOME adds the amount to accountId. EXPENSE subtracts it if sufficient funds exist. TRANSFER requires targetAccountId, subtracts from accountId, and adds to targetAccountId. The amount must be greater than zero, accountId/amount/type/date are required, and type is case-insensitive. The transaction and balance updates are committed together.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction created and account balance updated", content = @Content(schema = @Schema(implementation = Transaction.class), examples = @ExampleObject(value = "{\"id\":1,\"accountId\":1,\"categoryId\":null,\"amount\":5000.00,\"type\":\"INCOME\",\"targetAccountId\":null,\"description\":\"Salary\",\"date\":\"2026-10-01\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid transaction, unsupported type, missing transfer target, or insufficient balance", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"timestamp\":\"2026-01-01T12:00:00\",\"status\":400,\"error\":\"Insufficient balance in account\"}"))),
            @ApiResponse(responseCode = "404", description = "Source account or transfer target account was not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"timestamp\":\"2026-01-01T12:00:00\",\"status\":404,\"error\":\"Account not found: 42\"}")))
    })
    public ResponseEntity<Transaction> createTransaction(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Transaction details. Use INCOME, EXPENSE, or TRANSFER. For a transfer, supply targetAccountId. The date must be YYYY-MM-DD.", required = true,
                    content = @Content(schema = @Schema(implementation = Transaction.class), examples = {
                            @ExampleObject(name = "Income", summary = "Add income to an account", value = "{\"accountId\":1,\"amount\":5000.00,\"type\":\"INCOME\",\"description\":\"Salary\",\"date\":\"2026-10-01\"}"),
                            @ExampleObject(name = "Expense", summary = "Spend from an account", value = "{\"accountId\":1,\"amount\":1200.00,\"type\":\"EXPENSE\",\"categoryId\":2,\"description\":\"Groceries\",\"date\":\"2026-10-02\"}"),
                            @ExampleObject(name = "Transfer", summary = "Move money between accounts", value = "{\"accountId\":1,\"targetAccountId\":2,\"amount\":3000.00,\"type\":\"TRANSFER\",\"description\":\"Move to savings\",\"date\":\"2026-10-03\"}")
                    }))
            @Valid @RequestBody Transaction transaction) {
        return new ResponseEntity<>(transactionService.createTransaction(transaction), HttpStatus.CREATED);
    }
}
