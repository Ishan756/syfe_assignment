package com.financemanager.controller;

import com.financemanager.dto.request.GoalRequest;
import com.financemanager.dto.request.UpdateGoalRequest;
import com.financemanager.dto.response.GoalResponse;
import com.financemanager.entity.User;
import com.financemanager.service.AuthService;
import com.financemanager.service.SavingsGoalService;
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
class SavingsGoalControllerTest {

    @Mock
    private SavingsGoalService goalService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private SavingsGoalController savingsGoalController;

    @Test
    void createGoal_returnsCreatedGoal() {
        User user = new User();
        GoalRequest request = new GoalRequest();
        request.setGoalName("Emergency Fund");
        request.setTargetAmount(new BigDecimal("5000.00"));
        request.setTargetDate(LocalDate.of(2026, 1, 1));
        GoalResponse goalResponse = goalResponse();

        when(authService.getCurrentUser()).thenReturn(user);
        when(goalService.createGoal(request, user)).thenReturn(goalResponse);

        var result = savingsGoalController.createGoal(request);

        assertThat(result.getStatusCode().value()).isEqualTo(201);
        assertThat(result.getBody().getGoalName()).isEqualTo("Emergency Fund");
        verify(goalService).createGoal(request, user);
    }

    @Test
    void getAllGoals_returnsWrappedGoals() {
        User user = new User();
        GoalResponse goalResponse = goalResponse();
        when(authService.getCurrentUser()).thenReturn(user);
        when(goalService.getAllGoals(user)).thenReturn(Map.of("goals", List.of(goalResponse)));

        var result = savingsGoalController.getAllGoals();

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).containsKey("goals");
        verify(goalService).getAllGoals(user);
    }

    @Test
    void getGoal_returnsGoal() {
        User user = new User();
        GoalResponse goalResponse = goalResponse();
        when(authService.getCurrentUser()).thenReturn(user);
        when(goalService.getGoalById(1L, user)).thenReturn(goalResponse);

        var result = savingsGoalController.getGoal(1L);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody().getGoalName()).isEqualTo("Emergency Fund");
        verify(goalService).getGoalById(1L, user);
    }

    @Test
    void updateGoal_returnsUpdatedGoal() {
        User user = new User();
        UpdateGoalRequest request = new UpdateGoalRequest();
        request.setTargetAmount(new BigDecimal("6000.00"));
        request.setTargetDate(LocalDate.of(2026, 2, 1));
        GoalResponse goalResponse = goalResponse();

        when(authService.getCurrentUser()).thenReturn(user);
        when(goalService.updateGoal(1L, request, user)).thenReturn(goalResponse);

        var result = savingsGoalController.updateGoal(1L, request);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody().getGoalName()).isEqualTo("Emergency Fund");
        verify(goalService).updateGoal(1L, request, user);
    }

    @Test
    void deleteGoal_returnsSuccessMessage() {
        User user = new User();
        when(authService.getCurrentUser()).thenReturn(user);
        when(goalService.deleteGoal(1L, user)).thenReturn(Map.of("message", "Goal deleted successfully"));

        var result = savingsGoalController.deleteGoal(1L);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).containsEntry("message", "Goal deleted successfully");
        verify(goalService).deleteGoal(1L, user);
    }

    private GoalResponse goalResponse() {
        GoalResponse response = new GoalResponse();
        response.setId(1L);
        response.setGoalName("Emergency Fund");
        response.setTargetAmount(new BigDecimal("5000.00"));
        response.setTargetDate(LocalDate.of(2026, 1, 1));
        response.setStartDate(LocalDate.of(2025, 1, 1));
        response.setCurrentProgress(new BigDecimal("1000.00"));
        response.setProgressPercentage(20.0);
        response.setRemainingAmount(new BigDecimal("4000.00"));
        return response;
    }
}