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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository goalRepository;
    private final TransactionRepository transactionRepository;

    public SavingsGoalService(SavingsGoalRepository goalRepository, TransactionRepository transactionRepository) {
        this.goalRepository = goalRepository;
        this.transactionRepository = transactionRepository;
    }

    public GoalResponse createGoal(GoalRequest request, User user) {
        if (request.getTargetDate().isBefore(LocalDate.now()) || request.getTargetDate().isEqual(LocalDate.now())) {
            throw new BadRequestException("Target date must be in the future");
        }

        SavingsGoal goal = new SavingsGoal();
        goal.setGoalName(request.getGoalName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        goal.setStartDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now());
        goal.setUser(user);

        SavingsGoal saved = goalRepository.save(goal);
        return buildGoalResponse(saved, user);
    }

    public Map<String, List<GoalResponse>> getAllGoals(User user) {
        List<GoalResponse> goals = goalRepository.findByUser(user)
                .stream()
                .map(g -> buildGoalResponse(g, user))
                .collect(Collectors.toList());

        Map<String, List<GoalResponse>> response = new HashMap<>();
        response.put("goals", goals);
        return response;
    }

    public GoalResponse getGoalById(Long id, User user) {
        SavingsGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied");
        }

        return buildGoalResponse(goal, user);
    }

    public GoalResponse updateGoal(Long id, UpdateGoalRequest request, User user) {
        SavingsGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied");
        }

        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }
        if (request.getTargetDate() != null) {
            if (request.getTargetDate().isBefore(LocalDate.now()) || request.getTargetDate().isEqual(LocalDate.now())) {
                throw new BadRequestException("Target date must be in the future");
            }
            goal.setTargetDate(request.getTargetDate());
        }

        SavingsGoal saved = goalRepository.save(goal);
        return buildGoalResponse(saved, user);
    }

    public Map<String, String> deleteGoal(Long id, User user) {
        SavingsGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied");
        }

        goalRepository.delete(goal);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Goal deleted successfully");
        return response;
    }

    private GoalResponse buildGoalResponse(SavingsGoal goal, User user) {
        BigDecimal totalIncome = transactionRepository.sumIncomeByUserSince(user, goal.getStartDate());
        BigDecimal totalExpenses = transactionRepository.sumExpenseByUserSince(user, goal.getStartDate());

        BigDecimal currentProgress = totalIncome.subtract(totalExpenses);
        if (currentProgress.compareTo(BigDecimal.ZERO) < 0) {
            currentProgress = BigDecimal.ZERO;
        }

        BigDecimal remaining = goal.getTargetAmount().subtract(currentProgress);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        double percentage = 0.0;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentage = currentProgress.divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            if (percentage > 100.0) {
                percentage = 100.0;
            }
        }

        GoalResponse response = new GoalResponse();
        response.setId(goal.getId());
        response.setGoalName(goal.getGoalName());
        response.setTargetAmount(goal.getTargetAmount());
        response.setTargetDate(goal.getTargetDate());
        response.setStartDate(goal.getStartDate());
        response.setCurrentProgress(currentProgress.setScale(2, RoundingMode.HALF_UP));
        response.setProgressPercentage(Math.round(percentage * 100.0) / 100.0);
        response.setRemainingAmount(remaining.setScale(2, RoundingMode.HALF_UP));
        return response;
    }
}
