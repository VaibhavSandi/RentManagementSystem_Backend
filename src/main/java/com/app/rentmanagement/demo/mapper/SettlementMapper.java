package com.app.rentmanagement.demo.mapper;



import com.app.rentmanagement.demo.dto.SettlementDto;
import com.app.rentmanagement.demo.entity.Flat;
import com.app.rentmanagement.demo.entity.Renter;
import com.app.rentmanagement.demo.entity.Settlement;

import java.math.BigDecimal;

public class SettlementMapper {

    public static SettlementDto toDto(Settlement settlement) {
        if (settlement == null) return null;

        BigDecimal finalRefund = BigDecimal.ZERO;

        if (settlement.getDepositAmount() != null) {
            finalRefund = settlement.getDepositAmount();

            if (settlement.getPendingRent() != null) {
                finalRefund = finalRefund.subtract(settlement.getPendingRent());
            }

            if (settlement.getDeductionAmount() != null) {
                finalRefund = finalRefund.subtract(settlement.getDeductionAmount());
            }
        }

        return SettlementDto.builder()
                .settlementId(settlement.getSettlementId())
                .renterId(settlement.getRenter() != null ? settlement.getRenter().getRenterId() : null)
                .renterName(settlement.getRenter() != null ? settlement.getRenter().getRenterName() : null)
                .flatId(settlement.getFlat() != null ? settlement.getFlat().getFlatId() : null)
                .flatNo(settlement.getFlat() != null ? settlement.getFlat().getFlatNo() : null)
                .leavingDate(settlement.getLeavingDate())
                .depositAmount(settlement.getDepositAmount())
                .pendingRent(settlement.getPendingRent())
                .deductionAmount(settlement.getDeductionAmount())
                .deductionReason(settlement.getDeductionReason())
                .finalRefundAmount(finalRefund)
                .build();
    }

    public static Settlement toEntity(SettlementDto dto, Renter renter, Flat flat) {
        if (dto == null) return null;

        BigDecimal finalRefund = BigDecimal.ZERO;

        if (dto.getDepositAmount() != null) {
            finalRefund = dto.getDepositAmount();

            if (dto.getPendingRent() != null) {
                finalRefund = finalRefund.subtract(dto.getPendingRent());
            }

            if (dto.getDeductionAmount() != null) {
                finalRefund = finalRefund.subtract(dto.getDeductionAmount());
            }
        }

        return Settlement.builder()
                .settlementId(dto.getSettlementId())
                .renter(renter)
                .flat(flat)
                .leavingDate(dto.getLeavingDate())
                .depositAmount(dto.getDepositAmount())
                .pendingRent(dto.getPendingRent())
                .deductionAmount(dto.getDeductionAmount())
                .deductionReason(dto.getDeductionReason())
                .finalRefundAmount(finalRefund)
                .build();
    }
}
