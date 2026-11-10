package com.tengetracker.service;

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

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Transactional
    public Category createCategory(Category category) {
        category.setId(null);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, Category details) {
        Category category = getCategoryById(id);
        boolean typeChanges = !category.getType().equals(details.getType());
        if (typeChanges && (transactionRepository.existsByCategoryId(id) || budgetRepository.existsByCategoryId(id))) {
            throw new ConflictException("Cannot change the type of category " + id
                    + " because it is used by transactions or budgets");
        }
        category.setName(details.getName());
        category.setType(details.getType());
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        if (transactionRepository.existsByCategoryId(id) || budgetRepository.existsByCategoryId(id)) {
            throw new ConflictException("Category " + id + " is used by transactions or budgets and cannot be deleted");
        }
        categoryRepository.delete(category);
    }

    public List<Budget> getBudgetsByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        return budgetRepository.findByCategoryId(categoryId);
    }
}
