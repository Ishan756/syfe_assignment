package com.financemanager.controller;

import com.financemanager.dto.request.CategoryRequest;
import com.financemanager.dto.response.CategoryResponse;
import com.financemanager.entity.Category.CategoryType;
import com.financemanager.entity.User;
import com.financemanager.service.AuthService;
import com.financemanager.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    void getAllCategories_returnsWrappedCategories() {
        User user = new User();
        CategoryResponse response = new CategoryResponse(1L, "Salary", CategoryType.INCOME, false);
        when(authService.getCurrentUser()).thenReturn(user);
        when(categoryService.getAllCategories(user)).thenReturn(List.of(response));

        var result = categoryController.getAllCategories();

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).containsKey("categories");
        verify(categoryService).getAllCategories(user);
    }

    @Test
    void createCategory_returnsCreatedCategory() {
        User user = new User();
        CategoryRequest request = new CategoryRequest();
        request.setName("Travel");
        request.setType(CategoryType.EXPENSE);
        CategoryResponse response = new CategoryResponse(2L, "Travel", CategoryType.EXPENSE, true);

        when(authService.getCurrentUser()).thenReturn(user);
        when(categoryService.createCategory(request, user)).thenReturn(response);

        var result = categoryController.createCategory(request);

        assertThat(result.getStatusCode().value()).isEqualTo(201);
        assertThat(result.getBody().getName()).isEqualTo("Travel");
        verify(categoryService).createCategory(request, user);
    }

    @Test
    void deleteCategory_returnsSuccessMessage() {
        User user = new User();
        when(authService.getCurrentUser()).thenReturn(user);

        var result = categoryController.deleteCategory("Travel");

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).containsEntry("message", "Category deleted successfully");
        verify(categoryService).deleteCategory("Travel", user);
    }
}