package com.tengetracker.service;

import com.tengetracker.dto.SummaryResponse;
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

    public SummaryResponse getSummary(LocalDate startDate, LocalDate endDate) {
        BigDecimal totalIncome = transactionRepository.sumByTypeAndDateBetween("INCOME", startDate, endDate);
        BigDecimal totalExpenses = transactionRepository.sumByTypeAndDateBetween("EXPENSE", startDate, endDate);

        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        totalExpenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;

        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        BigDecimal totalAssets = accountRepository.findAll().stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .totalAssets(totalAssets)
                .build();
    }
}