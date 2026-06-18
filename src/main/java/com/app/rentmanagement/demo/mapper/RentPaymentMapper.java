package com.app.rentmanagement.demo.mapper;


import com.app.rentmanagement.demo.dto.RentPaymentDto;
import com.app.rentmanagement.demo.entity.Flat;
import com.app.rentmanagement.demo.entity.RentPayment;
import com.app.rentmanagement.demo.entity.Renter;

import java.math.BigDecimal;

public class RentPaymentMapper {

    public static RentPaymentDto toDto(RentPayment payment) {
        if (payment == null) return null;

        BigDecimal pendingAmount = BigDecimal.ZERO;

        if (payment.getMonthlyRent() != null && payment.getAmountPaid() != null) {
            pendingAmount = payment.getMonthlyRent().subtract(payment.getAmountPaid());
        }

        return RentPaymentDto.builder()
                .paymentId(payment.getPaymentId())
                .renterId(payment.getRenter() != null ? payment.getRenter().getRenterId() : null)
                .renterName(payment.getRenter() != null ? payment.getRenter().getRenterName() : null)
                .flatId(payment.getFlat() != null ? payment.getFlat().getFlatId() : null)
                .flatNo(payment.getFlat() != null ? payment.getFlat().getFlatNo() : null)
                .rentMonth(payment.getRentMonth())
                .rentYear(payment.getRentYear())
                .monthlyRent(payment.getMonthlyRent())
                .amountPaid(payment.getAmountPaid())
                .pendingAmount(pendingAmount)
                .paymentMode(payment.getPaymentMode())
                .remark(payment.getRemark())
                .paymentDate(payment.getPaymentDate())
                .build();
    }

    public static RentPayment toEntity(RentPaymentDto dto, Renter renter, Flat flat) {
        if (dto == null) return null;

        return RentPayment.builder()
                .paymentId(dto.getPaymentId())
                .renter(renter)
                .flat(flat)
                .rentMonth(dto.getRentMonth())
                .rentYear(dto.getRentYear())
                .monthlyRent(dto.getMonthlyRent())
                .amountPaid(dto.getAmountPaid())
                .paymentMode(dto.getPaymentMode())
                .remark(dto.getRemark())
                .paymentDate(dto.getPaymentDate())
                .build();
    }
}