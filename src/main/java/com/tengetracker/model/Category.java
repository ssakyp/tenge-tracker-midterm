package com.tengetracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Type must be either INCOME or EXPENSE")
    @Pattern(regexp = "^(INCOME|EXPENSE)$", message = "Type must be INCOME or EXPENSE")
    @Column(nullable = false)
    private String type;

    /** One-to-many: one category groups many transactions (inverse side, not serialized). */
    @OneToMany(mappedBy = "category")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();

    /** One-to-many: one category can have many monthly budgets (inverse side, not serialized). */
    @OneToMany(mappedBy = "category")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Budget> budgets = new ArrayList<>();
}
