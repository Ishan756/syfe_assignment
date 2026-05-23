package com.financemanager.repository;

import com.financemanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * JpaRepository: An interface provided by Spring Data JPA that provides standard CRUD
 * operations (Create, Read, Update, Delete) on the database without writing any SQL queries!
 * 
 * Generics:
 * - User: The Entity type this repository manages.
 * - Long: The data type of the Entity's primary key (User.id).
 * 
 * Spring automatically creates an implementation class for this interface at runtime 
 * and registers it as a Spring Bean (so we can @Autowire it in our services).
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Query Method: Spring Data JPA automatically parses this method name and generates 
     * the SQL query behind the scenes!
     * 
     * Code: findByUsername(String username)
     * SQL Equivalent: SELECT * FROM users WHERE username = ?
     * 
     * Optional<User>: A container object that may or may not contain a User. 
     * This prevents NullPointerExceptions if the user is not found.
     */
    Optional<User> findByUsername(String username);

    /**
     * Query Method: Spring Data JPA parses this to check if a record exists.
     * SQL Equivalent: SELECT count(*) > 0 FROM users WHERE username = ?
     */
    boolean existsByUsername(String username);
}
