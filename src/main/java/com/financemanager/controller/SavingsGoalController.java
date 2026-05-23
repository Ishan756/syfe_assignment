package com.financemanager.controller;

import com.financemanager.dto.request.GoalRequest;
import com.financemanager.dto.request.UpdateGoalRequest;
import com.financemanager.dto.response.GoalResponse;
import com.financemanager.entity.User;
import com.financemanager.service.AuthService;
import com.financemanager.service.SavingsGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
public class SavingsGoalController {

    private final SavingsGoalService goalService;
    private final AuthService authService;

    public SavingsGoalController(SavingsGoalService goalService, AuthService authService) {
        this.goalService = goalService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalRequest request) {
        User user = authService.getCurrentUser();
        GoalResponse response = goalService.createGoal(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, List<GoalResponse>>> getAllGoals() {
        User user = authService.getCurrentUser();
        Map<String, List<GoalResponse>> response = goalService.getAllGoals(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoal(@PathVariable Long id) {
        User user = authService.getCurrentUser();
        GoalResponse response = goalService.getGoalById(id, user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateGoalRequest request) {
        User user = authService.getCurrentUser();
        GoalResponse response = goalService.updateGoal(id, request, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(@PathVariable Long id) {
        User user = authService.getCurrentUser();
        Map<String, String> response = goalService.deleteGoal(id, user);
        return ResponseEntity.ok(response);
    }
}
