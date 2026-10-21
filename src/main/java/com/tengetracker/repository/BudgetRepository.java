package com.tengetracker.repository;

import com.tengetracker.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByCategoryIdAndMonthAndYear(Long categoryId, Integer month, Integer year);
    List<Budget> findByMonthAndYear(Integer month, Integer year);
}