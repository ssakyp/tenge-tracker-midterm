package com.tengetracker.repository;

import com.tengetracker.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, Integer month, Integer year);

    List<Budget> findByUserIdOrderByYearDescMonthDescIdAsc(Long userId);

    List<Budget> findByCategoryIdAndUserId(Long categoryId, Long userId);

    boolean existsByCategoryId(Long categoryId);
}
