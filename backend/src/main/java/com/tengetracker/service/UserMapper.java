package com.tengetracker.service;

import com.tengetracker.dto.auth.UserResponse;
import com.tengetracker.model.User;
import com.tengetracker.model.UserProfile;
import com.tengetracker.repository.AccountRepository;
import com.tengetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Converts the User entity to the safe UserResponse DTO (no password hash). */
@Component
@RequiredArgsConstructor
public class UserMapper {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public UserResponse toResponse(User user) {
        UserProfile p = user.getProfile();
        UserResponse.ProfileResponse profile = p == null ? null : UserResponse.ProfileResponse.builder()
                .fullName(p.getFullName())
                .email(p.getEmail())
                .preferredCurrency(p.getPreferredCurrency())
                .bio(p.getBio())
                .build();
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .createdAt(user.getCreatedAt())
                .profile(profile)
                .accountsCount(accountRepository.countByUserId(user.getId()))
                .transactionsCount(transactionRepository.countByAccount_UserId(user.getId()))
                .build();
    }
}
