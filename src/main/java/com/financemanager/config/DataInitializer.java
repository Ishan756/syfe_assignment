package com.financemanager.config;

import com.financemanager.entity.Category;
import com.financemanager.entity.Category.CategoryType;
import com.financemanager.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        seedDefaultCategory("Salary", CategoryType.INCOME);
        seedDefaultCategory("Food", CategoryType.EXPENSE);
        seedDefaultCategory("Rent", CategoryType.EXPENSE);
        seedDefaultCategory("Transportation", CategoryType.EXPENSE);
        seedDefaultCategory("Entertainment", CategoryType.EXPENSE);
        seedDefaultCategory("Healthcare", CategoryType.EXPENSE);
        seedDefaultCategory("Utilities", CategoryType.EXPENSE);
    }

    private void seedDefaultCategory(String name, CategoryType type) {
        if (!categoryRepository.existsByNameAndUserIsNull(name)) {
            Category category = new Category();
            category.setName(name);
            category.setType(type);
            category.setCustom(false);
            category.setUser(null);
            categoryRepository.save(category);
        }
    }
}
