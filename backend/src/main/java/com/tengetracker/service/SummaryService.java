package com.tengetracker.service;

import com.tengetracker.dto.SummaryResponse;
import com.tengetracker.exception.BadRequestException;
import com.tengetracker.model.Account;
import com.tengetracker.repository.AccountRepository;
import com.tengetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    /** Income/expense totals and assets of the current user only. */
    public SummaryResponse getSummary(LocalDate startDate, LocalDate endDate, Long userId) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("startDate must not be after endDate");
        }
        BigDecimal totalIncome = transactionRepository.sumByUserAndTypeAndDateBetween(userId, "INCOME", startDate, endDate);
        BigDecimal totalExpenses = transactionRepository.sumByUserAndTypeAndDateBetween(userId, "EXPENSE", startDate, endDate);

        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        totalExpenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;

        BigDecimal totalAssets = accountRepository.findByUserIdOrderByIdAsc(userId).stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(totalIncome.subtract(totalExpenses))
                .totalAssets(totalAssets)
                .build();
    }
}
