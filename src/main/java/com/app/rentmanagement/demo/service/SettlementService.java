package com.app.rentmanagement.demo.service;



import java.util.List;

import com.app.rentmanagement.demo.dto.RentPaymentDto;
import com.app.rentmanagement.demo.dto.RenterDto;
import com.app.rentmanagement.demo.dto.SettlementDto;

public interface SettlementService {

     List<RenterDto> getActiveRenters();

    List<RentPaymentDto> getLedger(Long renterId);

    SettlementDto saveSettlement(SettlementDto dto);

    List<SettlementDto> getAllSettlements();
}