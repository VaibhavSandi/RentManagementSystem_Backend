package com.app.rentmanagement.demo.service;



import com.app.rentmanagement.demo.dto.RenterDto;

import java.util.List;
import com.app.rentmanagement.demo.dto.RentRevisionDto;

public interface RenterService {



    RenterDto createRenter(RenterDto renterDto);

    RenterDto getRenterById(Long renterId);

    List<RenterDto> getAllRenters();

    RenterDto updateRenter(Long renterId, RenterDto renterDto);

    void deleteRenter(Long renterId);

    List<RenterDto> getAllActivateRenters();

    List<RentRevisionDto> getRentRevisions(Long renterId);

    RentRevisionDto addRentRevision(Long renterId, RentRevisionDto dto);
}
