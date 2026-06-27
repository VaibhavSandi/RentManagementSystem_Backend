package com.app.rentmanagement.demo.mapper;

import com.app.rentmanagement.demo.dto.ExpenseDto;
import com.app.rentmanagement.demo.entity.Expense;
import com.app.rentmanagement.demo.entity.Flat;

public class ExpenseMapper {

    public static ExpenseDto toDto(Expense expense) {
        if (expense == null) return null;

        return ExpenseDto.builder()
                .expenseId(expense.getExpenseId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .category(expense.getCategory())
                .flatId(expense.getFlat() != null ? expense.getFlat().getFlatId() : null)
                .flatNo(expense.getFlat() != null ? expense.getFlat().getFlatNo() : null)
                .build();
    }

    public static Expense toEntity(ExpenseDto dto, Flat flat) {
        if (dto == null) return null;

        return Expense.builder()
                .expenseId(dto.getExpenseId())
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .expenseDate(dto.getExpenseDate())
                .category(dto.getCategory())
                .flat(flat)
                .build();
    }
}
