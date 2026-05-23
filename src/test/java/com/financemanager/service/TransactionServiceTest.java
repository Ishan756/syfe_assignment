package com.financemanager.service;

import com.financemanager.dto.request.TransactionRequest;
import com.financemanager.dto.request.UpdateTransactionRequest;
import com.financemanager.dto.response.TransactionResponse;
import com.financemanager.entity.Category;
import com.financemanager.entity.Category.CategoryType;
import com.financemanager.entity.Transaction;
import com.financemanager.entity.User;
import com.financemanager.exception.BadRequestException;
import com.financemanager.exception.ResourceNotFoundException;
import com.financemanager.repository.CategoryRepository;
import com.financemanager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Category salaryCategory;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test@example.com");

        salaryCategory = new Category();
        salaryCategory.setId(1L);
        salaryCategory.setName("Salary");
        salaryCategory.setType(CategoryType.INCOME);
        salaryCategory.setCustom(false);

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(new BigDecimal("5000.00"));
        transaction.setDate(LocalDate.of(2024, 1, 15));
        transaction.setDescription("January Salary");
        transaction.setCategory(salaryCategory);
        transaction.setUser(user);
    }

    @Test
    void createTransaction_Success() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("5000.00"));
        request.setDate(LocalDate.now().minusDays(1));
        request.setCategory("Salary");
        request.setDescription("January Salary");

        when(categoryService.findCategoryByNameForUser("Salary", user)).thenReturn(salaryCategory);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse result = transactionService.createTransaction(request, user);

        assertEquals(new BigDecimal("5000.00"), result.getAmount());
        assertEquals("Salary", result.getCategory());
        assertEquals(CategoryType.INCOME, result.getType());
    }

    @Test
    void createTransaction_ThrowsBadRequest_WhenFutureDate() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setDate(LocalDate.now().plusDays(1));
        request.setCategory("Salary");

        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(request, user));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getTransactions_ReturnsAllUserTransactions() {
        when(transactionRepository.findByUserOrderByDateDesc(user)).thenReturn(List.of(transaction));

        Map<String, List<TransactionResponse>> result = transactionService.getTransactions(user, null, null, null);

        assertEquals(1, result.get("transactions").size());
    }

    @Test
    void getTransactions_FiltersByDateRange() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 1, 31);

        when(transactionRepository.findByUserAndDateBetweenOrderByDateDesc(user, start, end))
                .thenReturn(List.of(transaction));

        Map<String, List<TransactionResponse>> result = transactionService.getTransactions(user, start, end, null);

        assertEquals(1, result.get("transactions").size());
    }

    @Test
    void updateTransaction_Success() {
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setAmount(new BigDecimal("6000.00"));
        request.setDescription("Updated Salary");

        transaction.setAmount(new BigDecimal("6000.00"));
        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse result = transactionService.updateTransaction(1L, request, user);

        assertEquals(new BigDecimal("6000.00"), result.getAmount());
    }

    @Test
    void updateTransaction_ThrowsNotFound_WhenNotOwner() {
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setAmount(new BigDecimal("100.00"));

        when(transactionRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.updateTransaction(99L, request, user));
    }

    @Test
    void deleteTransaction_Success() {
        when(transactionRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(transaction));

        Map<String, String> result = transactionService.deleteTransaction(1L, user);

        assertEquals("Transaction deleted successfully", result.get("message"));
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void deleteTransaction_ThrowsNotFound_WhenNotExists() {
        when(transactionRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.deleteTransaction(99L, user));
    }
}
