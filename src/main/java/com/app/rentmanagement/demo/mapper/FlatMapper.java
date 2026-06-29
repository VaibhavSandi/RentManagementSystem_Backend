package com.app.rentmanagement.demo.mapper;


import com.app.rentmanagement.demo.dto.FlatDto;
import com.app.rentmanagement.demo.entity.Flat;

public class FlatMapper {

    public static FlatDto toDto(Flat flat) {
        if (flat == null) return null;

        return FlatDto.builder()
                .flatId(flat.getFlatId())
                .flatNo(flat.getFlatNo())
                .buildingName(flat.getBuildingName())
                .meterNo(flat.getMeterNo())
                .monthlyRent(flat.getMonthlyRent())
                .depositAmount(flat.getDepositAmount())
                .status(flat.getStatus())
                .build();
    }

    public static Flat toEntity(FlatDto dto) {
        if (dto == null) return null;

        return Flat.builder()
                .flatId(dto.getFlatId())
                .flatNo(dto.getFlatNo())
                .buildingName(dto.getBuildingName())
                .meterNo(dto.getMeterNo())
                .monthlyRent(dto.getMonthlyRent())
                .depositAmount(dto.getDepositAmount())
                .status(dto.getStatus())
                .build();
    }
}