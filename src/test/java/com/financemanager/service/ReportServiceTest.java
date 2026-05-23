package com.financemanager.service;

import com.financemanager.dto.response.MonthlyReportResponse;
import com.financemanager.dto.response.YearlyReportResponse;
import com.financemanager.entity.Category;
import com.financemanager.entity.Category.CategoryType;
import com.financemanager.entity.Transaction;
import com.financemanager.entity.User;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    private User user;
    private Transaction salaryTransaction;
    private Transaction foodTransaction;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        Category salary = new Category();
        salary.setName("Salary");
        salary.setType(CategoryType.INCOME);

        Category food = new Category();
        food.setName("Food");
        food.setType(CategoryType.EXPENSE);

        salaryTransaction = new Transaction();
        salaryTransaction.setAmount(new BigDecimal("3000.00"));
        salaryTransaction.setDate(LocalDate.of(2024, 1, 15));
        salaryTransaction.setCategory(salary);
        salaryTransaction.setUser(user);

        foodTransaction = new Transaction();
        foodTransaction.setAmount(new BigDecimal("400.00"));
        foodTransaction.setDate(LocalDate.of(2024, 1, 20));
        foodTransaction.setCategory(food);
        foodTransaction.setUser(user);
    }

    @Test
    void getMonthlyReport_CalculatesCorrectly() {
        when(transactionRepository.findByUserAndYearAndMonth(user, 2024, 1))
                .thenReturn(List.of(salaryTransaction, foodTransaction));

        MonthlyReportResponse report = reportService.getMonthlyReport(user, 2024, 1);

        assertEquals(2024, report.getYear());
        assertEquals(1, report.getMonth());
        assertEquals(new BigDecimal("3000.00"), report.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("400.00"), report.getTotalExpenses().get("Food"));
        assertEquals(new BigDecimal("2600.00"), report.getNetSavings());
    }

    @Test
    void getMonthlyReport_ReturnsZeroSavings_WhenNoTransactions() {
        when(transactionRepository.findByUserAndYearAndMonth(user, 2024, 1)).thenReturn(List.of());

        MonthlyReportResponse report = reportService.getMonthlyReport(user, 2024, 1);

        assertEquals(BigDecimal.ZERO, report.getNetSavings());
        assertTrue(report.getTotalIncome().isEmpty());
        assertTrue(report.getTotalExpenses().isEmpty());
    }

    @Test
    void getYearlyReport_AggregatesTotalsCorrectly() {
        when(transactionRepository.findByUserAndYear(user, 2024))
                .thenReturn(List.of(salaryTransaction, foodTransaction));

        YearlyReportResponse report = reportService.getYearlyReport(user, 2024);

        assertEquals(2024, report.getYear());
        assertEquals(new BigDecimal("3000.00"), report.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("400.00"), report.getTotalExpenses().get("Food"));
        assertEquals(new BigDecimal("2600.00"), report.getNetSavings());
    }

    @Test
    void getYearlyReport_GroupsByCategoryCorrectly() {
        Category salary = new Category();
        salary.setName("Salary");
        salary.setType(CategoryType.INCOME);

        Transaction secondSalary = new Transaction();
        secondSalary.setAmount(new BigDecimal("3000.00"));
        secondSalary.setDate(LocalDate.of(2024, 2, 15));
        secondSalary.setCategory(salary);

        when(transactionRepository.findByUserAndYear(user, 2024))
                .thenReturn(List.of(salaryTransaction, secondSalary));

        YearlyReportResponse report = reportService.getYearlyReport(user, 2024);

        assertEquals(new BigDecimal("6000.00"), report.getTotalIncome().get("Salary"));
    }
}
