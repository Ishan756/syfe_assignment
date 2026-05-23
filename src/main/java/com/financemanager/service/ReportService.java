package com.financemanager.service;

import com.financemanager.dto.response.MonthlyReportResponse;
import com.financemanager.dto.response.YearlyReportResponse;
import com.financemanager.entity.Category.CategoryType;
import com.financemanager.entity.Transaction;
import com.financemanager.entity.User;
import com.financemanager.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public MonthlyReportResponse getMonthlyReport(User user, int year, int month) {
        List<Transaction> transactions = transactionRepository.findByUserAndYearAndMonth(user, year, month);
        return buildMonthlyReport(transactions, year, month);
    }

    public YearlyReportResponse getYearlyReport(User user, int year) {
        List<Transaction> transactions = transactionRepository.findByUserAndYear(user, year);
        return buildYearlyReport(transactions, year);
    }

    private MonthlyReportResponse buildMonthlyReport(List<Transaction> transactions, int year, int month) {
        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        Map<String, BigDecimal> expenseByCategory = new HashMap<>();

        for (Transaction t : transactions) {
            String categoryName = t.getCategory().getName();
            if (t.getCategory().getType() == CategoryType.INCOME) {
                incomeByCategory.merge(categoryName, t.getAmount(), BigDecimal::add);
            } else {
                expenseByCategory.merge(categoryName, t.getAmount(), BigDecimal::add);
            }
        }

        BigDecimal totalIncome = incomeByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expenseByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        MonthlyReportResponse response = new MonthlyReportResponse();
        response.setMonth(month);
        response.setYear(year);
        response.setTotalIncome(incomeByCategory);
        response.setTotalExpenses(expenseByCategory);
        response.setNetSavings(netSavings);
        return response;
    }

    private YearlyReportResponse buildYearlyReport(List<Transaction> transactions, int year) {
        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        Map<String, BigDecimal> expenseByCategory = new HashMap<>();

        for (Transaction t : transactions) {
            String categoryName = t.getCategory().getName();
            if (t.getCategory().getType() == CategoryType.INCOME) {
                incomeByCategory.merge(categoryName, t.getAmount(), BigDecimal::add);
            } else {
                expenseByCategory.merge(categoryName, t.getAmount(), BigDecimal::add);
            }
        }

        BigDecimal totalIncome = incomeByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expenseByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        YearlyReportResponse response = new YearlyReportResponse();
        response.setYear(year);
        response.setTotalIncome(incomeByCategory);
        response.setTotalExpenses(expenseByCategory);
        response.setNetSavings(netSavings);
        return response;
    }
}
