package com.tengetracker.service;

import com.tengetracker.dto.BudgetStatusResponse;
import com.tengetracker.exception.BadRequestException;
import com.tengetracker.exception.ConflictException;
import com.tengetracker.exception.ResourceNotFoundException;
import com.tengetracker.model.Budget;
import com.tengetracker.model.Category;
import com.tengetracker.repository.BudgetRepository;
import com.tengetracker.repository.CategoryRepository;
import com.tengetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public List<Budget> getAllBudgets(Integer month, Integer year) {
        return budgetRepository.findAll().stream()
                .filter(b -> month == null || b.getMonth().equals(month))
                .filter(b -> year == null || b.getYear().equals(year))
                .toList();
    }

    public Budget getBudgetById(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
    }

    @Transactional
    public Budget createBudget(Budget budget) {
        budget.setId(null);
        validate(budget, null);
        return budgetRepository.save(budget);
    }

    @Transactional
    public Budget updateBudget(Long id, Budget details) {
        Budget budget = getBudgetById(id);
        validate(details, id);
        budget.setCategoryId(details.getCategoryId());
        budget.setLimitAmount(details.getLimitAmount());
        budget.setMonth(details.getMonth());
        budget.setYear(details.getYear());
        return budgetRepository.save(budget);
    }

    @Transactional
    public void deleteBudget(Long id) {
        budgetRepository.delete(getBudgetById(id));
    }

    public BudgetStatusResponse getBudgetStatus(Long id) {
        Budget budget = getBudgetById(id);
        YearMonth period = YearMonth.of(budget.getYear(), budget.getMonth());
        BigDecimal spent = transactionRepository.sumExpensesByCategoryAndDateBetween(
                budget.getCategoryId(), period.atDay(1), period.atEndOfMonth());
        if (spent == null) spent = BigDecimal.ZERO;

        return BudgetStatusResponse.builder()
                .budgetId(budget.getId())
                .categoryId(budget.getCategoryId())
                .limitAmount(budget.getLimitAmount())
                .spentAmount(spent)
                .remainingAmount(budget.getLimitAmount().subtract(spent))
                .isExceeded(spent.compareTo(budget.getLimitAmount()) > 0)
                .build();
    }

    private void validate(Budget budget, Long currentId) {
        Category category = categoryRepository.findById(budget.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + budget.getCategoryId()));
        if (!"EXPENSE".equals(category.getType())) {
            throw new BadRequestException("Budgets can only be set for EXPENSE categories");
        }
        budgetRepository.findByCategoryIdAndMonthAndYear(budget.getCategoryId(), budget.getMonth(), budget.getYear())
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ConflictException("A budget for this category and month/year already exists (id: " + existing.getId() + ")");
                });
    }
}
