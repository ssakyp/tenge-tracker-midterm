package com.tengetracker.service;

import com.tengetracker.dto.patch.CategoryPatchRequest;
import com.tengetracker.exception.ConflictException;
import com.tengetracker.exception.ResourceNotFoundException;
import com.tengetracker.model.Budget;
import com.tengetracker.model.Category;
import com.tengetracker.repository.BudgetRepository;
import com.tengetracker.repository.CategoryRepository;
import com.tengetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Categories are shared reference data: anyone can read them, logged-in users can change them. */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll(Sort.by("id"));
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
        ensureTypeCanChange(category, details.getType());
        category.setName(details.getName());
        category.setType(details.getType());
        return categoryRepository.save(category);
    }

    @Transactional
    public Category patchCategory(Long id, CategoryPatchRequest patch) {
        Category category = getCategoryById(id);
        if (patch.getType() != null) {
            ensureTypeCanChange(category, patch.getType());
            category.setType(patch.getType());
        }
        if (patch.getName() != null) category.setName(patch.getName().trim());
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

    /** Only the current user's budgets for this category. */
    public List<Budget> getBudgetsByCategory(Long categoryId, Long userId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        return budgetRepository.findByCategoryIdAndUserId(categoryId, userId);
    }

    private void ensureTypeCanChange(Category category, String newType) {
        boolean typeChanges = !category.getType().equals(newType);
        if (typeChanges && (transactionRepository.existsByCategoryId(category.getId())
                || budgetRepository.existsByCategoryId(category.getId()))) {
            throw new ConflictException("Cannot change the type of category " + category.getId()
                    + " because it is used by transactions or budgets");
        }
    }
}
