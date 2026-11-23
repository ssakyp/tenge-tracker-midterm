package com.tengetracker.service;

import com.tengetracker.dto.BudgetStatusResponse;
import com.tengetracker.dto.patch.BudgetPatchRequest;
import com.tengetracker.exception.BadRequestException;
import com.tengetracker.exception.ConflictException;
import com.tengetracker.exception.ForbiddenException;
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

/** Budgets are private to their owner. */
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public List<Budget> getAllBudgets(Integer month, Integer year, Long userId) {
        return budgetRepository.findByUserIdOrderByYearDescMonthDescIdAsc(userId).stream()
                .filter(b -> month == null || b.getMonth().equals(month))
                .filter(b -> year == null || b.getYear().equals(year))
                .toList();
    }

    public Budget getBudgetById(Long id, Long userId) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));
        if (!budget.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not have access to budget " + id);
        }
        return budget;
    }

    @Transactional
    public Budget createBudget(Budget budget, Long userId) {
        budget.setId(null);
        budget.setUserId(userId);
        validate(budget, null, userId);
        return budgetRepository.save(budget);
    }

    @Transactional
    public Budget updateBudget(Long id, Budget details, Long userId) {
        Budget budget = getBudgetById(id, userId);
        validate(details, id, userId);
        budget.setCategoryId(details.getCategoryId());
        budget.setLimitAmount(details.getLimitAmount());
        budget.setMonth(details.getMonth());
        budget.setYear(details.getYear());
        return budgetRepository.save(budget);
    }

    @Transactional
    public Budget patchBudget(Long id, BudgetPatchRequest patch, Long userId) {
        Budget existing = getBudgetById(id, userId);
        Budget merged = Budget.builder()
                .categoryId(patch.getCategoryId() != null ? patch.getCategoryId() : existing.getCategoryId())
                .limitAmount(patch.getLimitAmount() != null ? patch.getLimitAmount() : existing.getLimitAmount())
                .month(patch.getMonth() != null ? patch.getMonth() : existing.getMonth())
                .year(patch.getYear() != null ? patch.getYear() : existing.getYear())
                .build();
        return updateBudget(id, merged, userId);
    }

    @Transactional
    public void deleteBudget(Long id, Long userId) {
        budgetRepository.delete(getBudgetById(id, userId));
    }

    public BudgetStatusResponse getBudgetStatus(Long id, Long userId) {
        Budget budget = getBudgetById(id, userId);
        YearMonth period = YearMonth.of(budget.getYear(), budget.getMonth());
        BigDecimal spent = transactionRepository.sumUserExpensesByCategoryAndDateBetween(
                userId, budget.getCategoryId(), period.atDay(1), period.atEndOfMonth());
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

    private void validate(Budget budget, Long currentId, Long userId) {
        Category category = categoryRepository.findById(budget.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + budget.getCategoryId()));
        if (!"EXPENSE".equals(category.getType())) {
            throw new BadRequestException("Budgets can only be set for EXPENSE categories");
        }
        budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(userId, budget.getCategoryId(), budget.getMonth(), budget.getYear())
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ConflictException("A budget for this category and month/year already exists (id: " + existing.getId() + ")");
                });
    }
}
