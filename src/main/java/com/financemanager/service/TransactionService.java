package com.financemanager.service;

import com.financemanager.dto.request.TransactionRequest;
import com.financemanager.dto.request.UpdateTransactionRequest;
import com.financemanager.dto.response.TransactionResponse;
import com.financemanager.entity.Category;
import com.financemanager.entity.Transaction;
import com.financemanager.entity.User;
import com.financemanager.exception.BadRequestException;
import com.financemanager.exception.ResourceNotFoundException;
import com.financemanager.repository.CategoryRepository;
import com.financemanager.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository,
                               CategoryService categoryService,
                               CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.categoryRepository = categoryRepository;
    }

    public TransactionResponse createTransaction(TransactionRequest request, User user) {
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Date cannot be in the future");
        }

        Category category = categoryService.findCategoryByNameForUser(request.getCategory(), user);

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setDate(request.getDate());
        transaction.setDescription(request.getDescription());
        transaction.setCategory(category);
        transaction.setUser(user);

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    public Map<String, List<TransactionResponse>> getTransactions(User user, LocalDate startDate,
                                                                   LocalDate endDate, Long categoryId) {
        List<Transaction> transactions;

        if (startDate != null && endDate != null && categoryId != null) {
            Category cat = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            transactions = transactionRepository.findByUserAndDateBetweenAndCategoryOrderByDateDesc(
                    user, startDate, endDate, cat);
        } else if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate);
        } else if (categoryId != null) {
            Category cat = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            transactions = transactionRepository.findByUserAndCategoryOrderByDateDesc(user, cat);
        } else {
            transactions = transactionRepository.findByUserOrderByDateDesc(user);
        }

        Map<String, List<TransactionResponse>> response = new HashMap<>();
        response.put("transactions", transactions.stream().map(this::toResponse).collect(Collectors.toList()));
        return response;
    }

    public TransactionResponse updateTransaction(Long id, UpdateTransactionRequest request, User user) {
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            Category category = categoryService.findCategoryByNameForUser(request.getCategory(), user);
            transaction.setCategory(category);
        }

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    public Map<String, String> deleteTransaction(Long id, User user) {
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        transactionRepository.delete(transaction);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Transaction deleted successfully");
        return response;
    }

    public TransactionResponse toResponse(Transaction t) {
        TransactionResponse response = new TransactionResponse();
        response.setId(t.getId());
        response.setAmount(t.getAmount());
        response.setDate(t.getDate());
        response.setCategory(t.getCategory().getName());
        response.setDescription(t.getDescription());
        response.setType(t.getCategory().getType());
        return response;
    }
}
