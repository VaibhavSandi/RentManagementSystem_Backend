package com.app.rentmanagement.demo.service;



import com.app.rentmanagement.demo.dto.SettlementDto;

public interface SettlementService {

    SettlementDto createSettlement(SettlementDto settlementDto);

    SettlementDto getSettlementById(Long settlementId);

    SettlementDto getSettlementByRenterId(Long renterId);

    void deleteSettlement(Long settlementId);
}