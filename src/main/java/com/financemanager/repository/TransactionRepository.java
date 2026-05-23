package com.financemanager.repository;

import com.financemanager.entity.Category;
import com.financemanager.entity.Transaction;
import com.financemanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserOrderByDateDesc(User user);

    List<Transaction> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByUserAndCategoryOrderByDateDesc(User user, Category category);

    List<Transaction> findByUserAndDateBetweenAndCategoryOrderByDateDesc(
            User user, LocalDate startDate, LocalDate endDate, Category category);

    Optional<Transaction> findByIdAndUser(Long id, User user);

    boolean existsByCategory(Category category);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.category.type = 'INCOME' AND t.date >= :startDate")
    BigDecimal sumIncomeByUserSince(@Param("user") User user, @Param("startDate") LocalDate startDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.category.type = 'EXPENSE' AND t.date >= :startDate")
    BigDecimal sumExpenseByUserSince(@Param("user") User user, @Param("startDate") LocalDate startDate);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND YEAR(t.date) = :year AND MONTH(t.date) = :month ORDER BY t.date DESC")
    List<Transaction> findByUserAndYearAndMonth(
            @Param("user") User user, @Param("year") int year, @Param("month") int month);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND YEAR(t.date) = :year ORDER BY t.date DESC")
    List<Transaction> findByUserAndYear(@Param("user") User user, @Param("year") int year);
}
