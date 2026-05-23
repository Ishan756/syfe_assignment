package com.financemanager.service;

import com.financemanager.dto.request.CategoryRequest;
import com.financemanager.dto.response.CategoryResponse;
import com.financemanager.entity.Category;
import com.financemanager.entity.Category.CategoryType;
import com.financemanager.entity.User;
import com.financemanager.exception.BadRequestException;
import com.financemanager.exception.ConflictException;
import com.financemanager.exception.ForbiddenException;
import com.financemanager.repository.CategoryRepository;
import com.financemanager.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        User user = new User();
        Category category = new Category();
        category.setId(1L);
        category.setName("Salary");
        category.setType(CategoryType.INCOME);
        category.setCustom(false);

        when(categoryRepository.findByUserIsNullOrUser(user)).thenReturn(List.of(category));

        List<CategoryResponse> result = categoryService.getAllCategories(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Salary");
        assertThat(result.get(0).isCustom()).isFalse();
    }

    @Test
    void createCategory_success() {
        User user = new User();
        CategoryRequest request = new CategoryRequest();
        request.setName("SideBusiness");
        request.setType(CategoryType.INCOME);

        Category saved = new Category();
        saved.setId(42L);
        saved.setName("SideBusiness");
        saved.setType(CategoryType.INCOME);
        saved.setCustom(true);

        when(categoryRepository.existsByNameAndUserIsNull("SideBusiness")).thenReturn(false);
        when(categoryRepository.existsByNameAndUser("SideBusiness", user)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse response = categoryService.createCategory(request, user);

        assertThat(response.getId()).isEqualTo(42L);
        assertThat(response.getName()).isEqualTo("SideBusiness");
        assertThat(response.isCustom()).isTrue();
    }

    @Test
    void createCategory_throwsConflictWhenDefaultExists() {
        User user = new User();
        CategoryRequest request = new CategoryRequest();
        request.setName("Salary");
        request.setType(CategoryType.INCOME);

        when(categoryRepository.existsByNameAndUserIsNull("Salary")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request, user))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createCategory_throwsConflictWhenUserCategoryExists() {
        User user = new User();
        CategoryRequest request = new CategoryRequest();
        request.setName("SideBusiness");
        request.setType(CategoryType.INCOME);

        when(categoryRepository.existsByNameAndUserIsNull("SideBusiness")).thenReturn(false);
        when(categoryRepository.existsByNameAndUser("SideBusiness", user)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request, user))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deleteCategory_throwsForbiddenWhenDefaultCategory() {
        User user = new User();
        when(categoryRepository.existsByNameAndUserIsNull("Salary")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory("Salary", user))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void deleteCategory_throwsBadRequestWhenInUse() {
        User user = new User();
        Category category = new Category();
        category.setName("SideBusiness");
        category.setUser(user);

        when(categoryRepository.existsByNameAndUserIsNull("SideBusiness")).thenReturn(false);
        when(categoryRepository.findByNameAndUser("SideBusiness", user)).thenReturn(Optional.of(category));
        when(transactionRepository.existsByCategory(category)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory("SideBusiness", user))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void deleteCategory_success() {
        User user = new User();
        Category category = new Category();
        category.setName("SideBusiness");
        category.setUser(user);

        when(categoryRepository.existsByNameAndUserIsNull("SideBusiness")).thenReturn(false);
        when(categoryRepository.findByNameAndUser("SideBusiness", user)).thenReturn(Optional.of(category));
        when(transactionRepository.existsByCategory(category)).thenReturn(false);

        categoryService.deleteCategory("SideBusiness", user);

        verify(categoryRepository).delete(category);
    }
}
