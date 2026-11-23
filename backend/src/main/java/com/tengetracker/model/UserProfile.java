package com.tengetracker.model;

import jakarta.persistence.*;
import lombok.*;

/** One-to-one with User: personal details kept separately from credentials. */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owning side of the one-to-one; user_id is UNIQUE so a user cannot have two profiles. */
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(length = 120)
    private String email;

    @Column(name = "preferred_currency", length = 3, nullable = false)
    @Builder.Default
    private String preferredCurrency = "KZT";

    @Column(length = 255)
    private String bio;
}
