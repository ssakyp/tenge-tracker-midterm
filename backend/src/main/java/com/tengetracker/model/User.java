package com.tengetracker.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Application user. Passwords are stored only as BCrypt hashes.
 * Relationships:
 *  - one-to-one  User 1 -- 1 UserProfile
 *  - one-to-many User 1 --< N Account (and N Budget)
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    /** BCrypt hash, never the plain password and never serialized. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Incremented on every password change. Every JWT carries the version it was issued with,
     * so old tokens stop working as soon as the password changes.
     */
    @Column(name = "token_version", nullable = false)
    @Builder.Default
    private Integer tokenVersion = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** One-to-one: every user has exactly one profile (inverse side). */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile profile;

    /** One-to-many: a user owns many accounts (inverse side). */
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<Account> accounts = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (tokenVersion == null) tokenVersion = 0;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
        if (profile != null) profile.setUser(this);
    }
}
