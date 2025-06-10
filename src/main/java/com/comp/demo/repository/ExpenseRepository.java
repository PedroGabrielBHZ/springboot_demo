package com.comp.demo.repository;

import com.comp.demo.model.Expense;
import com.comp.demo.model.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUser(User user);

    Optional<Expense> findByIdAndUser(Long id, User user);
}