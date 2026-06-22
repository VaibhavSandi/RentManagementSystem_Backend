package com.app.rentmanagement.demo.service;


import java.util.List;

import com.app.rentmanagement.demo.dto.PendingRentDto;

public interface PendingRentService {

    List<PendingRentDto> getAllPendingRents();
}