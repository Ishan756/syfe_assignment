package com.financemanager.service;

import com.financemanager.dto.request.CategoryRequest;
import com.financemanager.dto.response.CategoryResponse;
import com.financemanager.entity.Category;
import com.financemanager.entity.User;
import com.financemanager.exception.BadRequestException;
import com.financemanager.exception.ConflictException;
import com.financemanager.exception.ForbiddenException;
import com.financemanager.exception.ResourceNotFoundException;
import com.financemanager.repository.CategoryRepository;
import com.financemanager.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public CategoryService(CategoryRepository categoryRepository, TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<CategoryResponse> getAllCategories(User user) {
        return categoryRepository.findByUserIsNullOrUser(user)
                .stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getType(), c.isCustom()))
                .collect(Collectors.toList());
    }

    public CategoryResponse createCategory(CategoryRequest request, User user) {
        if (categoryRepository.existsByNameAndUserIsNull(request.getName())) {
            throw new ConflictException("Category already exists");
        }
        if (categoryRepository.existsByNameAndUser(request.getName(), user)) {
            throw new ConflictException("Category already exists");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setType(request.getType());
        category.setCustom(true);
        category.setUser(user);

        Category saved = categoryRepository.save(category);
        return new CategoryResponse(saved.getId(), saved.getName(), saved.getType(), saved.isCustom());
    }

    public void deleteCategory(String name, User user) {
        if (categoryRepository.existsByNameAndUserIsNull(name)) {
            throw new ForbiddenException("Default categories cannot be deleted");
        }

        Category category = categoryRepository.findByNameAndUser(name, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (transactionRepository.existsByCategory(category)) {
            throw new BadRequestException("Category is in use and cannot be deleted");
        }

        categoryRepository.delete(category);
    }

    public Category findCategoryByNameForUser(String name, User user) {
        Category defaultCategory = categoryRepository.findByNameAndUserIsNull(name).orElse(null);
        if (defaultCategory != null) {
            return defaultCategory;
        }

        return categoryRepository.findByNameAndUser(name, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + name));
    }
}
