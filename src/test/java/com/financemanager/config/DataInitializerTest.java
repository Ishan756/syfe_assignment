package com.financemanager.config;

import com.financemanager.entity.Category;
import com.financemanager.entity.Category.CategoryType;
import com.financemanager.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void run_seedsDefaultCategoriesWhenMissing() throws Exception {
        when(categoryRepository.existsByNameAndUserIsNull("Salary")).thenReturn(false);
        when(categoryRepository.existsByNameAndUserIsNull("Food")).thenReturn(false);
        when(categoryRepository.existsByNameAndUserIsNull("Rent")).thenReturn(false);
        when(categoryRepository.existsByNameAndUserIsNull("Transportation")).thenReturn(false);
        when(categoryRepository.existsByNameAndUserIsNull("Entertainment")).thenReturn(false);
        when(categoryRepository.existsByNameAndUserIsNull("Healthcare")).thenReturn(false);
        when(categoryRepository.existsByNameAndUserIsNull("Utilities")).thenReturn(false);

        dataInitializer.run();

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(7)).save(captor.capture());

        assertThat(captor.getAllValues()).extracting(Category::getName)
                .containsExactly("Salary", "Food", "Rent", "Transportation", "Entertainment", "Healthcare", "Utilities");
        assertThat(captor.getAllValues()).allMatch(category -> !category.isCustom() && category.getUser() == null);
        assertThat(captor.getAllValues()).anyMatch(category -> category.getType() == CategoryType.INCOME);
    }
}