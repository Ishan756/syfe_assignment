package com.financemanager.service;

import com.financemanager.dto.request.GoalRequest;
import com.financemanager.dto.request.UpdateGoalRequest;
import com.financemanager.dto.response.GoalResponse;
import com.financemanager.entity.SavingsGoal;
import com.financemanager.entity.User;
import com.financemanager.exception.BadRequestException;
import com.financemanager.exception.ForbiddenException;
import com.financemanager.exception.ResourceNotFoundException;
import com.financemanager.repository.SavingsGoalRepository;
import com.financemanager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsGoalServiceTest {

    @Mock
    private SavingsGoalRepository goalRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private SavingsGoalService goalService;

    private User user;
    private SavingsGoal goal;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test@example.com");

        goal = new SavingsGoal();
        goal.setId(1L);
        goal.setGoalName("Emergency Fund");
        goal.setTargetAmount(new BigDecimal("5000.00"));
        goal.setTargetDate(LocalDate.now().plusMonths(6));
        goal.setStartDate(LocalDate.now());
        goal.setUser(user);
    }

    @Test
    void createGoal_Success() {
        GoalRequest request = new GoalRequest();
        request.setGoalName("Emergency Fund");
        request.setTargetAmount(new BigDecimal("5000.00"));
        request.setTargetDate(LocalDate.now().plusMonths(6));

        when(goalRepository.save(any(SavingsGoal.class))).thenReturn(goal);
        when(transactionRepository.sumIncomeByUserSince(any(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumExpenseByUserSince(any(), any())).thenReturn(BigDecimal.ZERO);

        GoalResponse result = goalService.createGoal(request, user);

        assertEquals("Emergency Fund", result.getGoalName());
        assertEquals(new BigDecimal("5000.00"), result.getTargetAmount());
    }

    @Test
    void createGoal_ThrowsBadRequest_WhenPastTargetDate() {
        GoalRequest request = new GoalRequest();
        request.setGoalName("Test Goal");
        request.setTargetAmount(new BigDecimal("1000.00"));
        request.setTargetDate(LocalDate.now().minusDays(1));

        assertThrows(BadRequestException.class, () -> goalService.createGoal(request, user));
    }

    @Test
    void createGoal_SetsStartDateToToday_WhenNotProvided() {
        GoalRequest request = new GoalRequest();
        request.setGoalName("Test Goal");
        request.setTargetAmount(new BigDecimal("1000.00"));
        request.setTargetDate(LocalDate.now().plusMonths(3));
        request.setStartDate(null);

        when(goalRepository.save(any(SavingsGoal.class))).thenAnswer(inv -> {
            SavingsGoal g = inv.getArgument(0);
            g.setId(1L);
            assertEquals(LocalDate.now(), g.getStartDate());
            return g;
        });
        when(transactionRepository.sumIncomeByUserSince(any(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.sumExpenseByUserSince(any(), any())).thenReturn(BigDecimal.ZERO);

        goalService.createGoal(request, user);
    }

    @Test
    void getGoalById_ThrowsForbidden_WhenDifferentUser() {
        User otherUser = new User();
        otherUser.setId(2L);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThrows(ForbiddenException.class, () -> goalService.getGoalById(1L, otherUser));
    }

    @Test
    void deleteGoal_Success() {
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));

        Map<String, String> result = goalService.deleteGoal(1L, user);

        assertEquals("Goal deleted successfully", result.get("message"));
        verify(goalRepository).delete(goal);
    }

    @Test
    void deleteGoal_ThrowsNotFound() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> goalService.deleteGoal(99L, user));
    }

    @Test
    void progressCalculation_IsCorrect() {
        when(goalRepository.save(any(SavingsGoal.class))).thenReturn(goal);
        when(transactionRepository.sumIncomeByUserSince(any(), any())).thenReturn(new BigDecimal("1000.00"));
        when(transactionRepository.sumExpenseByUserSince(any(), any())).thenReturn(new BigDecimal("0.00"));

        GoalRequest request = new GoalRequest();
        request.setGoalName("Emergency Fund");
        request.setTargetAmount(new BigDecimal("5000.00"));
        request.setTargetDate(LocalDate.now().plusMonths(6));

        GoalResponse result = goalService.createGoal(request, user);

        assertEquals(new BigDecimal("1000.00"), result.getCurrentProgress());
        assertEquals(20.0, result.getProgressPercentage());
        assertEquals(new BigDecimal("4000.00"), result.getRemainingAmount());
    }
}
