package com.comp.demo.controller;

import com.comp.demo.model.Expense;
import com.comp.demo.model.User;
import com.comp.demo.payload.request.ExpenseRequest;
import com.comp.demo.repository.ExpenseRepository;
import com.comp.demo.repository.UserRepository;
import com.comp.demo.security.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/expenses")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')") // All expense endpoints require user or admin role
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }

    @Operation(summary = "Get all expenses for the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of expenses retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<Expense>> getAllExpenses() {
        User currentUser = getCurrentUser();
        List<Expense> expenses = expenseRepository.findByUser(currentUser);
        return ResponseEntity.ok(expenses);
    }

    @Operation(summary = "Get an expense by ID for the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Expense found"),
        @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@Parameter(description = "ID of the expense to retrieve") @PathVariable Long id) {
        User currentUser = getCurrentUser();
        Optional<Expense> expense = expenseRepository.findByIdAndUser(id, currentUser);
        return expense.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new expense for the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Expense created successfully")
    })
    @PostMapping
    public ResponseEntity<Expense> createExpense(@Valid @RequestBody ExpenseRequest expenseRequest) {
        User currentUser = getCurrentUser();
        Expense expense = new Expense();
        expense.setDescription(expenseRequest.getDescription());
        expense.setAmount(expenseRequest.getAmount());
        expense.setDate(expenseRequest.getDate());
        expense.setUser(currentUser); // Associate expense with the current user
        Expense savedExpense = expenseRepository.save(expense);
        return new ResponseEntity<>(savedExpense, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing expense for the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Expense updated successfully"),
        @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@Parameter(description = "ID of the expense to update") @PathVariable Long id, @Valid @RequestBody ExpenseRequest expenseRequest) {
        User currentUser = getCurrentUser();
        return expenseRepository.findByIdAndUser(id, currentUser)
                .map(expense -> {
                    expense.setDescription(expenseRequest.getDescription());
                    expense.setAmount(expenseRequest.getAmount());
                    expense.setDate(expenseRequest.getDate());
                    // User association cannot be changed
                    Expense updatedExpense = expenseRepository.save(expense);
                    return ResponseEntity.ok(updatedExpense);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete an expense for the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Expense deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteExpense(@Parameter(description = "ID of the expense to delete") @PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (expenseRepository.findByIdAndUser(id, currentUser).isPresent()) {
            expenseRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}