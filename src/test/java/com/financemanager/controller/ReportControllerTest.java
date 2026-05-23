package com.financemanager.controller;

import com.financemanager.dto.response.MonthlyReportResponse;
import com.financemanager.dto.response.YearlyReportResponse;
import com.financemanager.entity.User;
import com.financemanager.service.AuthService;
import com.financemanager.service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private ReportController reportController;

    @Test
    void getMonthlyReport_returnsMonthlyReport() {
        User user = new User();
        MonthlyReportResponse response = new MonthlyReportResponse();
        response.setMonth(1);
        response.setYear(2024);
        response.setTotalIncome(Map.of("Salary", new BigDecimal("3000.00")));
        response.setTotalExpenses(Map.of("Food", new BigDecimal("400.00")));
        response.setNetSavings(new BigDecimal("2600.00"));

        when(authService.getCurrentUser()).thenReturn(user);
        when(reportService.getMonthlyReport(user, 2024, 1)).thenReturn(response);

        var result = reportController.getMonthlyReport(2024, 1);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody().getYear()).isEqualTo(2024);
        verify(reportService).getMonthlyReport(user, 2024, 1);
    }

    @Test
    void getYearlyReport_returnsYearlyReport() {
        User user = new User();
        YearlyReportResponse response = new YearlyReportResponse();
        response.setYear(2024);
        response.setTotalIncome(Map.of("Salary", new BigDecimal("36000.00")));
        response.setTotalExpenses(Map.of("Food", new BigDecimal("4800.00")));
        response.setNetSavings(new BigDecimal("31200.00"));

        when(authService.getCurrentUser()).thenReturn(user);
        when(reportService.getYearlyReport(user, 2024)).thenReturn(response);

        var result = reportController.getYearlyReport(2024);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody().getYear()).isEqualTo(2024);
        verify(reportService).getYearlyReport(user, 2024);
    }
}