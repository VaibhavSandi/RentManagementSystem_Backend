package com.app.rentmanagement.demo.serviceImpl;


import com.app.rentmanagement.demo.dto.SettlementDto;
import com.app.rentmanagement.demo.entity.Flat;
import com.app.rentmanagement.demo.entity.Renter;
import com.app.rentmanagement.demo.entity.Settlement;
import com.app.rentmanagement.demo.exception.ResourceNotFoundException;
import com.app.rentmanagement.demo.repository.FlatRepository;
import com.app.rentmanagement.demo.repository.RenterRepository;
import com.app.rentmanagement.demo.repository.SettlementRepository;
import com.app.rentmanagement.demo.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final RenterRepository renterRepository;
    private final FlatRepository flatRepository;

    @Override
    public SettlementDto createSettlement(SettlementDto dto) {

        Renter renter = renterRepository.findById(dto.getRenterId())
                .orElseThrow(() -> new ResourceNotFoundException("Renter", "renterId", dto.getRenterId()));

        Flat flat = flatRepository.findById(dto.getFlatId())
                .orElseThrow(() -> new ResourceNotFoundException("Flat", "flatId", dto.getFlatId()));

        BigDecimal depositAmount = safe(dto.getDepositAmount());
        BigDecimal pendingRent = safe(dto.getPendingRent());
        BigDecimal deductionAmount = safe(dto.getDeductionAmount());

        BigDecimal finalRefundAmount = depositAmount
                .subtract(pendingRent)
                .subtract(deductionAmount);

        Settlement settlement = Settlement.builder()
                .renter(renter)
                .flat(flat)
                .leavingDate(dto.getLeavingDate())
                .depositAmount(depositAmount)
                .pendingRent(pendingRent)
                .deductionAmount(deductionAmount)
                .deductionReason(dto.getDeductionReason())
                .finalRefundAmount(finalRefundAmount)
                .build();

        Settlement savedSettlement = settlementRepository.save(settlement);

        return mapToDto(savedSettlement);
    }

    @Override
    public SettlementDto getSettlementById(Long settlementId) {

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement", "settlementId", settlementId));

        return mapToDto(settlement);
    }

    @Override
    public SettlementDto getSettlementByRenterId(Long renterId) {

        Settlement settlement = settlementRepository.findByRenterRenterId(renterId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement", "renterId", renterId));

        return mapToDto(settlement);
    }

    @Override
    public void deleteSettlement(Long settlementId) {

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement", "settlementId", settlementId));

        settlementRepository.delete(settlement);
    }

    private SettlementDto mapToDto(Settlement settlement) {

        return SettlementDto.builder()
                .settlementId(settlement.getSettlementId())
                .renterId(settlement.getRenter().getRenterId())
                .renterName(settlement.getRenter().getRenterName())
                .flatId(settlement.getFlat().getFlatId())
                .flatNo(settlement.getFlat().getFlatNo())
                .leavingDate(settlement.getLeavingDate())
                .depositAmount(settlement.getDepositAmount())
                .pendingRent(settlement.getPendingRent())
                .deductionAmount(settlement.getDeductionAmount())
                .deductionReason(settlement.getDeductionReason())
                .finalRefundAmount(settlement.getFinalRefundAmount())
                .build();
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}