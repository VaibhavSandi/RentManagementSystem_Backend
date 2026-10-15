package com.app.rentmanagement.demo.service;

import com.app.rentmanagement.demo.dto.DashboardDto;

import java.time.YearMonth;

public interface DashboardService {

    /** @param rentMonth period for the period figures; null = latest due rent month */
    public DashboardDto getDashboardData(YearMonth rentMonth);
}
