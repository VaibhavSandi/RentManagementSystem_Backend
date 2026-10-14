package com.app.rentmanagement.demo.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.app.rentmanagement.demo.dto.RentPaymentDto;
import com.app.rentmanagement.demo.dto.RenterDto;
import com.app.rentmanagement.demo.dto.SettlementDto;
import com.app.rentmanagement.demo.entity.Flat;
import com.app.rentmanagement.demo.entity.Renter;
import com.app.rentmanagement.demo.entity.Settlement;
import com.app.rentmanagement.demo.exception.ResourceNotFoundException;
import com.app.rentmanagement.demo.mapper.RentPaymentMapper;
import com.app.rentmanagement.demo.mapper.RenterMapper;
import com.app.rentmanagement.demo.mapper.SettlementMapper;
import com.app.rentmanagement.demo.repository.FlatRepository;
import com.app.rentmanagement.demo.repository.RentPaymentRepository;
import com.app.rentmanagement.demo.repository.RenterRepository;
import com.app.rentmanagement.demo.repository.SettlementRepository;
import com.app.rentmanagement.demo.service.SettlementService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final RentPaymentRepository rentPaymentRepository;
    private final RenterRepository renterRepository;
    private final FlatRepository flatRepository;

    @Override
    public List<RenterDto> getActiveRenters() {

        return renterRepository.findByStatus("Active")
                .stream()
                .map(RenterMapper::toDto)
                .toList();
    }

    @Override
    public List<RentPaymentDto> getLedger(Long renterId) {

        return rentPaymentRepository
                .findByRenterRenterIdOrderByPaymentDateAsc(renterId)
                .stream()
                .map(RentPaymentMapper::toDto)
                .toList();
    }

    @Override
    public SettlementDto saveSettlement(SettlementDto dto) {

        Renter renter = renterRepository.findById(dto.getRenterId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Renter",
                                "renterId",
                                dto.getRenterId()));

        Flat flat = flatRepository.findById(dto.getFlatId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Flat",
                                "flatId",
                                dto.getFlatId()));

        Settlement settlement = Settlement.builder()
                .renter(renter)
                .flat(flat)
                .leavingDate(dto.getLeavingDate())
                .depositAmount(dto.getDepositAmount())
                .pendingRent(dto.getPendingRent())
                .deductionAmount(dto.getDeductionAmount())
                .deductionReason(dto.getDeductionReason())
                .finalRefundAmount(dto.getFinalRefundAmount())
                .build();

        settlementRepository.save(settlement);

        renter.setStatus("INACTIVE");
        renterRepository.save(renter);

        flat.setStatus("Vacant");
        flatRepository.save(flat);

        return SettlementMapper.toDto(settlement);
    }

    @Override
    public List<SettlementDto> getAllSettlements() {

        return settlementRepository.findAll()
                .stream()
                .map(SettlementMapper::toDto)
                .toList();
    }
}