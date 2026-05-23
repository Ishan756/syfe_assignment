package com.financemanager.service;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void getAllCategories_returnsMappedResponses() {
        Category c = new Category();
        c.setId(1L);
        c.setName("Salary");
        c.setType(CategoryType.INCOME);
        c.setCustom(false);

        User user = new User();

        when(categoryRepository.findByUserIsNullOrUser(user)).thenReturn(List.of(c));

        List<CategoryResponse> result = categoryService.getAllCategories(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Salary");
        assertThat(result.get(0).isCustom()).isFalse();
    }

    @Test
    void createCategory_throwsWhenDefaultExists() {
        User user = new User();
        when(categoryRepository.existsByNameAndUserIsNull("Salary")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(
                new com.financemanager.dto.request.CategoryRequest() {{ setName("Salary"); setType(CategoryType.INCOME); }}, user))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createCategory_success() {
        User user = new User();
        com.financemanager.dto.request.CategoryRequest req = new com.financemanager.dto.request.CategoryRequest();
        req.setName("SideBusiness");
        req.setType(CategoryType.INCOME);

        when(categoryRepository.existsByNameAndUserIsNull("SideBusiness")).thenReturn(false);
        when(categoryRepository.existsByNameAndUser("SideBusiness", user)).thenReturn(false);

        Category saved = new Category();
        saved.setId(42L);
        saved.setName("SideBusiness");
        saved.setType(CategoryType.INCOME);
        saved.setCustom(true);

        when(categoryRepository.save(any())).thenReturn(saved);

        CategoryResponse response = categoryService.createCategory(req, user);

        assertThat(response.getName()).isEqualTo("SideBusiness");
        assertThat(response.isCustom()).isTrue();
        assertThat(response.getId()).isEqualTo(42L);
    }

    @Test
    void deleteCategory_forbiddenIfDefault() {
        User user = new User();
        when(categoryRepository.existsByNameAndUserIsNull("Salary")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory("Salary", user))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void deleteCategory_success() {
        User user = new User();
        Category cat = new Category();
        cat.setId(7L);
        cat.setName("Custom");
        cat.setUser(user);

        when(categoryRepository.existsByNameAndUserIsNull("Custom")).thenReturn(false);
        when(categoryRepository.findByNameAndUser("Custom", user)).thenReturn(Optional.of(cat));
        when(transactionRepository.existsByCategory(cat)).thenReturn(false);

        categoryService.deleteCategory("Custom", user);

        verify(categoryRepository).delete(cat);
    }

    @Test
    void deleteCategory_throwsWhenInUse() {
        User user = new User();
        Category cat = new Category();
        cat.setId(9L);
        cat.setName("Custom");
        cat.setUser(user);

        when(categoryRepository.existsByNameAndUserIsNull("Custom")).thenReturn(false);
        when(categoryRepository.findByNameAndUser("Custom", user)).thenReturn(Optional.of(cat));
        when(transactionRepository.existsByCategory(cat)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory("Custom", user))
                .isInstanceOf(BadRequestException.class);
    }
}
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
