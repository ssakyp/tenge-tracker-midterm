package com.tengetracker.service;

import com.tengetracker.exception.BadRequestException;
import com.tengetracker.exception.ResourceNotFoundException;
import com.tengetracker.model.Account;
import com.tengetracker.model.Transaction;
import com.tengetracker.repository.AccountRepository;
import com.tengetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        Account sourceAccount = accountRepository.findById(transaction.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + transaction.getAccountId()));

        String type = transaction.getType().toUpperCase();
        transaction.setType(type);

        switch (type) {
            case "INCOME":
                sourceAccount.setBalance(sourceAccount.getBalance().add(transaction.getAmount()));
                break;

            case "EXPENSE":
                if (sourceAccount.getBalance().compareTo(transaction.getAmount()) < 0) {
                    throw new BadRequestException("Insufficient balance in account");
                }
                sourceAccount.setBalance(sourceAccount.getBalance().subtract(transaction.getAmount()));
                break;

            case "TRANSFER":
                if (transaction.getTargetAccountId() == null) {
                    throw new BadRequestException("Target account ID is required for transfers");
                }
                Account targetAccount = accountRepository.findById(transaction.getTargetAccountId())
                        .orElseThrow(() -> new ResourceNotFoundException("Target account not found: " + transaction.getTargetAccountId()));

                if (sourceAccount.getBalance().compareTo(transaction.getAmount()) < 0) {
                    throw new BadRequestException("Insufficient balance in source account");
                }

                sourceAccount.setBalance(sourceAccount.getBalance().subtract(transaction.getAmount()));
                targetAccount.setBalance(targetAccount.getBalance().add(transaction.getAmount()));
                accountRepository.save(targetAccount);
                break;

            default:
                throw new BadRequestException("Invalid transaction type: " + type);
        }

        accountRepository.save(sourceAccount);
        return transactionRepository.save(transaction);
    }
}