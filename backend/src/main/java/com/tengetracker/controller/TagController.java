package com.tengetracker.controller;

import com.tengetracker.dto.patch.TagPatchRequest;
import com.tengetracker.model.Transaction;
import com.tengetracker.security.AuthUser;
import com.tengetracker.service.TagService;
import com.tengetracker.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Labels for transactions (many-to-many). GET is public; changes need a token.")
public class TagController {

    private final TagService tagService;
    private final TransactionService transactionService;

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "List tags (public)")
    public List<com.tengetracker.model.Tag> getAllTags() {
        return tagService.getAllTags();
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "Get a tag by ID (public)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag found"),
            @ApiResponse(responseCode = "404", description = "Tag not found")
    })
    public com.tengetracker.model.Tag getTag(@PathVariable Long id) {
        return tagService.getTagById(id);
    }

    @PostMapping
    @Operation(summary = "Create a tag")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tag created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "409", description = "Tag name already exists")
    })
    public ResponseEntity<com.tengetracker.model.Tag> createTag(@Valid @RequestBody com.tengetracker.model.Tag tag) {
        return new ResponseEntity<>(tagService.createTag(tag), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a tag")
    public com.tengetracker.model.Tag updateTag(@PathVariable Long id, @Valid @RequestBody com.tengetracker.model.Tag tag) {
        return tagService.updateTag(id, tag);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a tag", description = "Only name and/or color that are present are changed.")
    public com.tengetracker.model.Tag patchTag(@PathVariable Long id, @Valid @RequestBody TagPatchRequest patch) {
        return tagService.patchTag(id, patch);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a tag", description = "Removes the tag from all transactions, then deletes it.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tag deleted"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Tag not found")
    })
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "List my transactions with this tag", description = "Many-to-many navigation; only the current user's transactions.")
    public List<Transaction> getTagTransactions(@PathVariable Long id,
                                                @Parameter(hidden = true) @AuthenticationPrincipal AuthUser me) {
        return transactionService.getTransactionsByTag(id, me.id());
    }
}
