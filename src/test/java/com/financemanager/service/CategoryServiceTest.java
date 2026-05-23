package com.financemanager.service;

import com.financemanager.dto.request.CategoryRequest;
import com.financemanager.dto.response.CategoryResponse;
import com.financemanager.entity.Category;
import com.financemanager.entity.Category.CategoryType;
import com.financemanager.entity.User;
import com.financemanager.exception.BadRequestException;
import com.financemanager.exception.ConflictException;
import com.financemanager.exception.ForbiddenException;
import com.financemanager.exception.ResourceNotFoundException;
import com.financemanager.repository.CategoryRepository;
import com.financemanager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User user;
    private Category defaultCategory;
    private Category customCategory;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test@example.com");

        defaultCategory = new Category();
        defaultCategory.setId(1L);
        defaultCategory.setName("Salary");
        defaultCategory.setType(CategoryType.INCOME);
        defaultCategory.setCustom(false);
        defaultCategory.setUser(null);

        customCategory = new Category();
        customCategory.setId(2L);
        customCategory.setName("Freelance");
        customCategory.setType(CategoryType.INCOME);
        customCategory.setCustom(true);
        customCategory.setUser(user);
    }

    @Test
    void getAllCategories_ReturnsAllCategories() {
        when(categoryRepository.findByUserIsNullOrUser(user)).thenReturn(List.of(defaultCategory, customCategory));

        List<CategoryResponse> result = categoryService.getAllCategories(user);

        assertEquals(2, result.size());
    }

    @Test
    void createCategory_Success() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Freelance");
        request.setType(CategoryType.INCOME);

        when(categoryRepository.existsByNameAndUserIsNull(anyString())).thenReturn(false);
        when(categoryRepository.existsByNameAndUser(anyString(), any())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(customCategory);

        CategoryResponse result = categoryService.createCategory(request, user);

        assertEquals("Freelance", result.getName());
        assertTrue(result.isCustom());
    }

    @Test
    void createCategory_ThrowsConflict_WhenDefaultExists() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Salary");
        request.setType(CategoryType.INCOME);

        when(categoryRepository.existsByNameAndUserIsNull("Salary")).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.createCategory(request, user));
    }

    @Test
    void createCategory_ThrowsConflict_WhenUserCategoryExists() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Freelance");
        request.setType(CategoryType.INCOME);

        when(categoryRepository.existsByNameAndUserIsNull(anyString())).thenReturn(false);
        when(categoryRepository.existsByNameAndUser("Freelance", user)).thenReturn(true);

        assertThrows(ConflictException.class, () -> categoryService.createCategory(request, user));
    }

    @Test
    void deleteCategory_ThrowsForbidden_WhenDefaultCategory() {
        when(categoryRepository.existsByNameAndUserIsNull("Salary")).thenReturn(true);

        assertThrows(ForbiddenException.class, () -> categoryService.deleteCategory("Salary", user));
    }

    @Test
    void deleteCategory_ThrowsBadRequest_WhenInUse() {
        when(categoryRepository.existsByNameAndUserIsNull("Freelance")).thenReturn(false);
        when(categoryRepository.findByNameAndUser("Freelance", user)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategory(customCategory)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.deleteCategory("Freelance", user));
    }

    @Test
    void deleteCategory_Success() {
        when(categoryRepository.existsByNameAndUserIsNull("Freelance")).thenReturn(false);
        when(categoryRepository.findByNameAndUser("Freelance", user)).thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategory(customCategory)).thenReturn(false);

        categoryService.deleteCategory("Freelance", user);

        verify(categoryRepository).delete(customCategory);
    }

    @Test
    void deleteCategory_ThrowsNotFound_WhenCategoryNotFound() {
        when(categoryRepository.existsByNameAndUserIsNull("Unknown")).thenReturn(false);
        when(categoryRepository.findByNameAndUser("Unknown", user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory("Unknown", user));
    }
}
