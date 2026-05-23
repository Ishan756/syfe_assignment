package com.financemanager.repository;

import com.financemanager.entity.Category;
import com.financemanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserIsNullOrUser(User user);

    Optional<Category> findByNameAndUserIsNull(String name);

    Optional<Category> findByNameAndUser(String name, User user);

    boolean existsByNameAndUser(String name, User user);

    boolean existsByNameAndUserIsNull(String name);
}
