package com.tengetracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/** Free-form label. Many-to-many with Transaction through the transaction_tags join table. */
@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tag {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Schema(example = "recurring")
    @NotBlank(message = "Tag name is required")
    @Size(max = 30, message = "Tag name must be at most 30 characters")
    @Column(nullable = false, unique = true, length = 30)
    private String name;

    @Schema(example = "#2563eb", description = "Hex color used by the frontend")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a hex value like #2563eb")
    @Column(length = 7)
    @Builder.Default
    private String color = "#64748b";

    /** Many-to-many (inverse side): transactions labelled with this tag. */
    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    @ToString.Exclude
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();
}
