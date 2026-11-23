package com.tengetracker.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Public view of a user: never contains the password hash. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private LocalDateTime createdAt;
    private ProfileResponse profile;
    private long accountsCount;
    private long transactionsCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileResponse {
        private String fullName;
        private String email;
        private String preferredCurrency;
        private String bio;
    }
}
