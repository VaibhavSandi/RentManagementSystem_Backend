package com.app.rentmanagement.demo.serviceImpl;

import com.app.rentmanagement.demo.dto.ExpenseDto;
import com.app.rentmanagement.demo.entity.Expense;
import com.app.rentmanagement.demo.entity.Flat;
import com.app.rentmanagement.demo.exception.ResourceNotFoundException;
import com.app.rentmanagement.demo.mapper.ExpenseMapper;
import com.app.rentmanagement.demo.repository.ExpenseRepository;
import com.app.rentmanagement.demo.repository.FlatRepository;
import com.app.rentmanagement.demo.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final FlatRepository flatRepository;

    @Override
    public ExpenseDto createExpense(ExpenseDto expenseDto) {
        Flat flat = null;
        if (expenseDto.getFlatId() != null && expenseDto.getFlatId() != 0) {
            flat = flatRepository.findById(expenseDto.getFlatId())
                    .orElseThrow(() -> new ResourceNotFoundException("Flat", "flatId", expenseDto.getFlatId()));
        }

        Expense expense = ExpenseMapper.toEntity(expenseDto, flat);
        Expense savedExpense = expenseRepository.save(expense);
        return ExpenseMapper.toDto(savedExpense);
    }

    @Override
    public ExpenseDto getExpenseById(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "expenseId", expenseId));
        return ExpenseMapper.toDto(expense);
    }

    @Override
    public List<ExpenseDto> getAllExpenses() {
        return expenseRepository.findAll()
                .stream()
                .map(ExpenseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ExpenseDto updateExpense(Long expenseId, ExpenseDto expenseDto) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "expenseId", expenseId));

        Flat flat = null;
        if (expenseDto.getFlatId() != null && expenseDto.getFlatId() != 0) {
            flat = flatRepository.findById(expenseDto.getFlatId())
                    .orElseThrow(() -> new ResourceNotFoundException("Flat", "flatId", expenseDto.getFlatId()));
        }

        expense.setDescription(expenseDto.getDescription());
        expense.setAmount(expenseDto.getAmount());
        expense.setExpenseDate(expenseDto.getExpenseDate());
        expense.setCategory(expenseDto.getCategory());
        expense.setFlat(flat);

        Expense updatedExpense = expenseRepository.save(expense);
        return ExpenseMapper.toDto(updatedExpense);
    }

    @Override
    public void deleteExpense(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "expenseId", expenseId));
        expenseRepository.delete(expense);
    }
}
