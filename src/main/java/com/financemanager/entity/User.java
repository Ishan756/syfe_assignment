package com.financemanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @Entity: Specifies that this Java class is a JPA entity, meaning it maps directly to a 
 * database table. Hibernate (the default JPA provider) will manage this class's lifecycle.
 * 
 * @Table(name = "users"): Explicitly specifies the name of the database table this class maps to. 
 * We use "users" because "user" is a reserved keyword in some databases (like PostgreSQL).
 * 
 * Lombok Annotations:
 * - @Getter: Automatically generates getter methods for all fields at compile time.
 * - @Setter: Automatically generates setter methods for all fields at compile time.
 * - @NoArgsConstructor: Generates a default parameterless constructor required by JPA.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    /**
     * @Id: Marks this field as the primary key of the database table.
     * @GeneratedValue: Specifies how the primary key should be generated.
     * GenerationType.IDENTITY: The database will automatically auto-increment the ID column (e.g., using a serial in PostgreSQL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column: Configures details about the database column for this field.
    // unique = true: No two users can have the same username.
    // nullable = false: Username is mandatory and cannot be NULL.
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String phoneNumber;

    /**
     * @OneToMany: Represents a one-to-many relationship in the database (one User has many Transactions).
     * mappedBy = "user": Points to the 'user' field in the Transaction class, establishing a bi-directional relationship.
     * cascade = CascadeType.ALL: Operations (like persist, merge, remove) performed on the User will cascade to their transactions.
     * orphanRemoval = true: If a transaction is removed from the user's transactions list, it is also deleted from the database.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavingsGoal> savingsGoals = new ArrayList<>();
}
