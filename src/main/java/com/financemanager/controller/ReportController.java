package com.financemanager.controller;

import com.financemanager.dto.response.MonthlyReportResponse;
import com.financemanager.dto.response.YearlyReportResponse;
import com.financemanager.entity.User;
import com.financemanager.service.AuthService;
import com.financemanager.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final AuthService authService;

    public ReportController(ReportService reportService, AuthService authService) {
        this.reportService = reportService;
        this.authService = authService;
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(@PathVariable int year, @PathVariable int month) {
        User user = authService.getCurrentUser();
        MonthlyReportResponse response = reportService.getMonthlyReport(user, year, month);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(@PathVariable int year) {
        User user = authService.getCurrentUser();
        YearlyReportResponse response = reportService.getYearlyReport(user, year);
        return ResponseEntity.ok(response);
    }
}
