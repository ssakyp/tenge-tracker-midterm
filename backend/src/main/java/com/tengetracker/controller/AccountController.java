package com.tengetracker.controller;

import com.tengetracker.dto.patch.AccountPatchRequest;
import com.tengetracker.model.Account;
import com.tengetracker.model.Transaction;
import com.tengetracker.security.AuthUser;
import com.tengetracker.service.AccountService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "The current user's financial accounts (User 1 -> N Account). All endpoints need a token; another user's account returns 403.")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "List my accounts")
    public List<Account> getAllAccounts(@Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return accountService.getAllAccounts(me.id());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one of my accounts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "403", description = "Account belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public Account getAccountById(@PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return accountService.getAccountById(id, me.id());
    }

    @PostMapping
    @Operation(summary = "Create an account", description = "Body: name (required), balance (>= 0, default 0), currency (3 letters, default KZT). The owner is taken from the JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<Account> createAccount(@Valid @RequestBody Account account,
                                                 @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return new ResponseEntity<>(accountService.createAccount(account, me.id()), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace an account's name and currency", description = "The balance is never changed here; it changes only through transactions.")
    public Account updateAccount(@PathVariable Long id, @Valid @RequestBody Account account,
                                 @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return accountService.updateAccount(id, account, me.id());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update an account", description = "Send only name and/or currency.")
    public Account patchAccount(@PathVariable Long id, @Valid @RequestBody AccountPatchRequest patch,
                                @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return accountService.patchAccount(id, patch, me.id());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an account", description = "409 if the account still has transactions.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "403", description = "Account belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "409", description = "Account has transactions")
    })
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        accountService.deleteAccount(id, me.id());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "List an account's transactions", description = "Account 1 -> N Transaction (as source or transfer target).")
    public List<Transaction> getAccountTransactions(@PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return transactionService.getTransactionsByAccount(id, me.id());
    }
}
