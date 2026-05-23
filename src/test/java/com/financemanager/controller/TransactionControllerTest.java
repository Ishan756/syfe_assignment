package com.financemanager.controller;

import com.financemanager.dto.request.TransactionRequest;
import com.financemanager.dto.request.UpdateTransactionRequest;
import com.financemanager.dto.response.TransactionResponse;
import com.financemanager.entity.Category.CategoryType;
import com.financemanager.entity.User;
import com.financemanager.service.AuthService;
import com.financemanager.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    void createTransaction_returnsCreatedTransaction() {
        User user = new User();
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setDate(LocalDate.of(2024, 1, 15));
        request.setCategory("Salary");
        request.setDescription("January Salary");
        TransactionResponse response = transactionResponse();

        when(authService.getCurrentUser()).thenReturn(user);
        when(transactionService.createTransaction(request, user)).thenReturn(response);

        var result = transactionController.createTransaction(request);

        assertThat(result.getStatusCode().value()).isEqualTo(201);
        assertThat(result.getBody().getCategory()).isEqualTo("Salary");
        verify(transactionService).createTransaction(request, user);
    }

    @Test
    void getTransactions_returnsWrappedTransactions() {
        User user = new User();
        TransactionResponse response = transactionResponse();
        when(authService.getCurrentUser()).thenReturn(user);
        when(transactionService.getTransactions(user, null, null, null)).thenReturn(Map.of("transactions", List.of(response)));

        var result = transactionController.getTransactions(null, null, null);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).containsKey("transactions");
        verify(transactionService).getTransactions(user, null, null, null);
    }

    @Test
    void updateTransaction_returnsUpdatedTransaction() {
        User user = new User();
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setAmount(new BigDecimal("150.00"));
        request.setDescription("Updated Salary");
        TransactionResponse response = transactionResponse();

        when(authService.getCurrentUser()).thenReturn(user);
        when(transactionService.updateTransaction(1L, request, user)).thenReturn(response);

        var result = transactionController.updateTransaction(1L, request);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody().getAmount()).isEqualByComparingTo("100.00");
        verify(transactionService).updateTransaction(1L, request, user);
    }

    @Test
    void deleteTransaction_returnsSuccessMessage() {
        User user = new User();
        when(authService.getCurrentUser()).thenReturn(user);
        when(transactionService.deleteTransaction(1L, user)).thenReturn(Map.of("message", "Transaction deleted successfully"));

        var result = transactionController.deleteTransaction(1L);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).containsEntry("message", "Transaction deleted successfully");
        verify(transactionService).deleteTransaction(1L, user);
    }

    private TransactionResponse transactionResponse() {
        TransactionResponse response = new TransactionResponse();
        response.setId(1L);
        response.setAmount(new BigDecimal("100.00"));
        response.setDate(LocalDate.of(2024, 1, 15));
        response.setCategory("Salary");
        response.setDescription("January Salary");
        response.setType(CategoryType.INCOME);
        return response;
    }
}